package com.wei.demo.mq;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

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
    public void start() {
        producer = new DefaultMQProducer("default-producer-group");
        producer.setNamesrvAddr(nameAddr);
        producer.setRetryTimesWhenSendFailed(10);
        try {
            producer.start();
        } catch (MQClientException e) {
            throw new RuntimeException("start mq producer failed " + e);
        }

    }

    public void shudown() {
        producer.shutdown();
    }

    public void send(String topic, String tag, byte[] body) {
        Message msg = new Message(topic, tag, body);
        try {
            producer.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("send mq failed " + e);
        }
    }

    /**
     * 发送顺序消息
     * @param topic
     * @param tag
     * @param body
     * @param id 唯一区分用户的id
     */
    public void sendOrderMessage(String topic, String tag, byte[] body, long id) {
        Message msg = new Message(topic, tag, body);
        try {
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    long orderId = (long) arg;
                    int index = (int) orderId % mqs.size();
                    return mqs.get(index);
                }
            }, id);
        } catch (Exception e) {
            throw new RuntimeException("send mq failed " + e);
        }
    }

}
