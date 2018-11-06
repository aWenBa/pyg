package com.pyg.page.service.impl;

import com.pyg.mapper.TbGoodsDescMapper;
import com.pyg.mapper.TbGoodsMapper;
import com.pyg.mapper.TbItemCatMapper;
import com.pyg.mapper.TbItemMapper;
import com.pyg.page.service.ItemPageService;
import com.pyg.pojo.TbGoods;
import com.pyg.pojo.TbGoodsDesc;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {


    //加载配置文件
    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //创建配置类
            Configuration configuration = freeMarkerConfig.getConfiguration();
            //加载模板
            Template template = configuration.getTemplate("item.ftl");
            //创建模板使用数据集
            Map dataModel = new HashMap();
            System.out.println("**************************************************");
            addData(dataModel, goodsId);
            //创建Writer对象
            Writer writer = new FileWriter(pagedir + goodsId + ".html");
            //输出静态资源页面
            template.process(dataModel, writer);
            //关闭流
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long goodsId : goodsIds) {
                //删除静态页面
                new File(pagedir + goodsId + ".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //向数据模板填入数据
    private void addData(Map dataModel, Long goodsId) {
        //1.加载商品表数据
        TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goods", goods);
        //2.加载商品扩展表数据
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
        dataModel.put("goodsDesc", goodsDesc);
        //3.获取商品三级分类
        String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
        String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
        String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
        dataModel.put("itemCat1", itemCat1);
        dataModel.put("itemCat2", itemCat2);
        dataModel.put("itemCat3", itemCat3);
        //4.加载SKU列表
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//状态为有效
        criteria.andGoodsIdEqualTo(goodsId);//指定SPU Id
        example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
        List<TbItem> itemList = itemMapper.selectByExample(example);
        dataModel.put("itemList", itemList);
    }
}
