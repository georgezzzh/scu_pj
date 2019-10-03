package com.sonydafa.pj.service;

import com.google.gson.Gson;
import com.sonydafa.pj.domain.UserJob;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class Consumer implements MessageListener {
    @Autowired
    private Productor productor;
    @Override
    public void onMessage(Message message) {
        //System.out.println("消费者的消息:" + new String(message.getBody()));
        UserJob userJob = new Gson().fromJson(new String(message.getBody()), UserJob.class);
        Map<String, String> cookieMap = new HashMap<>();
        cookieMap.put("JSESSIONID", userJob.getScuSession());
        long lastUpdateTime = userJob.getLastUpdateTime();
        long now = new Date().getTime();
        long diff = now - lastUpdateTime;
        userJob.setLastUpdateTime(now);
        String userJobJsonUpdate = new Gson().toJson(userJob);
        //时间差未到，重新加入队列中
        if (diff < 125 * 1000) {
            productor.sendMessage("sessionId", new String(message.getBody()));
        }
        if (diff > (125) * 1000) {
            try {
                int itemNumber=-1;
                itemNumber = ScuRequest.submitOne(cookieMap);
                if (itemNumber > 0)
                    productor.sendMessage("sessionId", userJobJsonUpdate);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("submit Error");
            }
            //该数据未处理完，重新加入队列中

        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
