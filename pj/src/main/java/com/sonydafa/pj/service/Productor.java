package com.sonydafa.pj.service;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Productor{
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendMessage(String routingKey,Object message){
        //System.out.println("send some message:"+message.toString());
        amqpTemplate.convertAndSend(routingKey,message);
        //RabbitAdmin admin=new RabbitAdmin();
        //admin.getRabbitTemplate().execute()
    }
}
