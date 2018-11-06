package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));
        Map<String, Object> map = new HashMap<>();
        //1.按关键字查询（高亮显示，等）
        map.putAll(searchList(searchMap));
        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
//        System.out.println(categoryList.get(0));
        map.put("categoryList", categoryList);
        //3.根据商品分类查询品牌和规格列表
        if (categoryList.size() > 0) {
            map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
        }

        return map;
    }

    //从缓存中查询商品品牌以及规格列表
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //根据商品所属分类获取模板ID
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
//        System.out.println(typeId);
        if (typeId != null) {
            //根据模板ID获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
//            System.out.println(brandList);
            map.put("brandList", brandList);
            //根据模板ID获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
//            System.out.println(specList);
            map.put("specList", specList);
        }
        return map;
    }

    //查询分类列表
    private List searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<>();
        Query query = new SimpleFacetQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项，也就是根据什么分组
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //根据查询条件，返回分类的分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
//        System.out.println(groupResult);
        //分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //分组结果入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            //将分组结果的名称封装到返回值中
//            System.out.println(entry.getGroupValue());
            list.add(entry.getGroupValue());
        }
        return list;
    }

    //根据关键字搜索，进行查询
    private Map searchList(Map searchMap) {
        Map<String, Object> map = new HashMap<>();
        //创建查询条件对象
        HighlightQuery query = new SimpleHighlightQuery();
        //设置高亮查询区域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);
        //1.按照关键字查找
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //2.添加其他过滤条件
        //2.2定义分类过滤
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //2.3定义品牌过滤
        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //2.4规格过滤
        Object spec = searchMap.get("spec");
        if (spec != null) {
            Map<String, String> specMap = (Map<String, String>) spec;
            for (String key : specMap.keySet()) {
                Criteria categoryFilterCriteria = new Criteria("item_spec_" + key).is(searchMap.get(key));
                FilterQuery categoryFilterQuery = new SimpleFilterQuery(categoryFilterCriteria);
                query.addFilterQuery(categoryFilterQuery);
            }
        }
        //2.5按照价格筛选
        if (!"".equals(searchMap.get("price"))) {
            String[] price = ((String) searchMap.get("price")).split("-");
            //区间起点不为0，设置价格的最低索引
            if (!price[0].equals("0")) {
                Criteria priceFilterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery priceFilterQuery = new SimpleFilterQuery(priceFilterCriteria);
                query.addFilterQuery(priceFilterQuery);
            }
            //区间终点不为*，设置价格的最高索引
            if (!price[1].equals("*")) {
                Criteria priceFilterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery priceFilterQuery = new SimpleFilterQuery(priceFilterCriteria);
                query.addFilterQuery(priceFilterQuery);
            }
        }
        //2.6分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");//获取前端传过来的当前页码
        //默认为第一页
        if (pageNo == null) {
            pageNo = 1;
        }
        //获取每页记录数
        Integer pageSize = (Integer) searchMap.get("pageSize");
        //默认每页记录20条
        if (pageSize == null) {
            pageSize = 20;
        }
        //设置每页记录数
        query.setRows(pageSize);
        //从第几条查询
        query.setOffset((pageNo - 1) * pageSize);

        //2.7排序
        //ASC DESC
        String sortValue = (String) searchMap.get("sort");
        //排序字段
        String sortField = (String) searchMap.get("sortField");
        if (sortValue != null && !sortValue.equals("")) {
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }


        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //2.1高亮查询
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
        for (HighlightEntry<TbItem> tbItemHighlightEntry : highlighted) {
            //获取原实体类
            TbItem entity = tbItemHighlightEntry.getEntity();
            if (tbItemHighlightEntry.getHighlights().size() > 0 && tbItemHighlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                entity.setTitle(tbItemHighlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());
        //返回查询总页数
        map.put("totalPages", page.getTotalPages());
        //返回总查询数
        map.put("total", page.getTotalElements());
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
