package com.wei.demo.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wei.demo.constant.RedisConstant;
import com.wei.demo.constant.TempUserConstant;
import com.wei.demo.entity.Stock;
import com.wei.demo.mq.Consumer;
import com.wei.demo.mq.Producer;
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
    private Producer producer;

    @Autowired
    private Consumer consumer;

    @Autowired
    private IStockService stockService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void initConsumer() {
        consumer.subscribe("dec:stock:", null, null, new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt message : list) {
                    try {
                        Stock stock = JSON.parseObject(message.getBody().toString(), new TypeReference<Stock>() {
                        });
                        int updateNum = stockService.updateStockById(stock);
                        if (updateNum == 0) {
                            throw new RuntimeException("更新库存失败！");
                        }
                        stock.setSale(stock.getSale() + 1);
                        stock.setVersion(stock.getVersion() + 1);
                        redisTemplate.opsForValue().set(RedisConstant.STOCK_KEY_PREFIX + stock.getId(), JSON.toJSONString(stock));
                        producer.sendOrderMessage("incr:order:",null,JSON.toJSONBytes(stock), TempUserConstant.USER_ID);
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
