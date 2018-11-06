package com.pyg.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class itemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        System.out.println("监听接受到消息。。。");
        try {
            //1.读取接受的消息
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            List<TbItem> list = JSON.parseArray(text, TbItem.class);
            //2.调用业务方法，完成逻辑
            itemSearchService.importList(list);
            System.out.println("监听消费消息。。。");

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
