package com.wei.demo.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wei.demo.constant.RedisConstant;
import com.wei.demo.entity.Order;
import com.wei.demo.entity.Stock;
import com.wei.demo.mq.Consumer;
import com.wei.demo.mq.Producer;
import com.wei.demo.service.IOrderService;
import com.wei.demo.service.IStockService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author weiwenfeng
 * @date 2019/4/18
 */
@Component
public class MessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);
    @Autowired
    private Consumer consumer;

    @Autowired
    private IOrderService orderService;

    @PostConstruct
    public void initConsumer() {
        consumer.subscribe("incr:order", null, null, new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt message : list) {
                    try {
                        Stock stock = JSON.parseObject(message.getBody().toString(), new TypeReference<Stock>() {
                        });
                        Order order = new Order();
                        order.setSid(stock.getId());
                        order.setName(stock.getName());
                        orderService.createOrder(order);

                        /**
                         *
                         * to do 异步回调通知
                         *
                         */
                    } catch (RuntimeException e) {
                        log.error("Consume message fail：topic = dec:stock:,fail time = " + message.getReconsumeTimes(), e);
                        return message.getReconsumeTimes() > 10 ? ConsumeConcurrentlyStatus.CONSUME_SUCCESS :
                                ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
    }
}
