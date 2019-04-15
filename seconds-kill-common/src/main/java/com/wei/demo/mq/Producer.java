package com.wei.demo.mq;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author: weiwenfeng
 * @Date: 2018/11/5
 */
@Component
public class Producer {

    @Value("${apache.rocketmq.namesrvAddr}")
    private String nameAddr;

    private DefaultMQProducer producer;

    @PostConstruct
    public void start(){
        producer = new DefaultMQProducer("default-producer-group");
        producer.setNamesrvAddr(nameAddr);
        producer.setRetryTimesWhenSendFailed(10);
        try {
            producer.start();
        } catch (MQClientException e) {
            throw new RuntimeException("start mq producer failed " + e);
        }

    }

    public void shudown(){
        producer.shutdown();
    }

    public void send(String topic,String tag,byte[] body){
        Message msg  = new Message(topic,tag,body);
        try {
            producer.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("send mq failed " + e);
        }
    }

}
