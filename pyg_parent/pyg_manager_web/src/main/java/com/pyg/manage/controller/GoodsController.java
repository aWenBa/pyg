package com.pyg.manage.controller;

import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
//import com.pyg.page.service.ItemPageService;
import com.pyg.pojo.TbItem;
//import com.pyg.search.service.ItemSearchService;
import entity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbGoods;
import com.pyg.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

//    @Reference
//    private ItemSearchService itemSearchService;

//    @Reference
//    private ItemPageService itemPageService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination queueSolrDestination;//用于发送solr导入的信息

    @Autowired
    private Destination queueSolrDeleteDestination;//用户在索引库中删除的记录

    @Autowired
    private Destination topicPageDestination;//获取广播信息

    @Autowired
    private Destination topicPageDeleteDestination;//用于删除静态网页的消息


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 增加
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods) {
        try {
            goodsService.add(goods);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            //删除solr索引库
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            //删除静态网页
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
//            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    //更改状态
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            System.out.println(status);
            for (Long id : ids) {
                System.out.println(id);
            }
            //根据SKU的ID查询SKU的列表（状态为“1”）
            if (status.equals("1")) {//审核通过
                List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
                System.out.println(itemList);
                System.out.println(itemList.size());
                //调用接口实现数据导入
                if (itemList.size() > 0) {
                    String jsonString = JSON.toJSONString(itemList);
                    //更新solr索引库
                    jmsTemplate.send(queueSolrDestination, session -> session.createTextMessage(jsonString));
//                    itemSearchService.importList(itemList);
                } else {
                    System.out.println("无明细数据");
                }
                //生成静态页面
                for (Long id : ids) {
                    jmsTemplate.send(topicPageDestination, session -> session.createObjectMessage(id));
//                    itemPageService.genItemHtml(id);
                }
            }
            return new Result(true, "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, "操作失败");

        }
    }

    //生成静态页面（测试）
    @RequestMapping("/genHtml")
    public void genHtml(long goodsId) {
        try {
            System.out.println("--------------");
            jmsTemplate.send(topicPageDestination, session -> session.createObjectMessage(goodsId));
            System.out.println("==============");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
