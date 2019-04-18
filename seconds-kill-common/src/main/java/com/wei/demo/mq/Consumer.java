package com.wei.demo.mq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: weiwenfeng
 * @Date: 2018/11/5
 */
@Component
public class Consumer {

    @Value("${apache.rocketmq.namesrvAddr}")
    private String nameAddr;

    /**
     *
     * @param topic 订阅话题
     * @param tags 订阅tags
     * @param groupName 消费组名
     * @param messageListener 消息监听器
     */
    public void subscribe(String topic, String tags, String groupName, MessageListenerConcurrently messageListener){
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(nameAddr);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.registerMessageListener(messageListener);
        try {
            consumer.subscribe(topic,tags);
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        try {
            consumer.start();
        } catch (MQClientException e) {
            throw new RuntimeException("start rocketmq consumer failed " + e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                consumer.shutdown();
            }
        });
    }
}
