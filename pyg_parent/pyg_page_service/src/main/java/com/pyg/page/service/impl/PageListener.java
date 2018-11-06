package com.pyg.page.service.impl;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;


    @Override
    public void onMessage(Message message) {
        try {
            //1. 读取到消息
            System.out.println("监听lllllllllllll到消息");
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long id = (Long) objectMessage.getObject();
            //2. 调用业务逻辑: 生成静态页面
            itemPageService.genItemHtml(id);
            System.out.println("消费完消息");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
