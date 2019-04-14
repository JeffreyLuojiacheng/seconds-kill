package com.wei.demo.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wei.demo.annotation.RateLimit;
import com.wei.demo.entity.Order;
import com.wei.demo.entity.Stock;
import com.wei.demo.service.IOrderService;
import com.wei.demo.service.IStockService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@RestController
@RequestMapping("/kill")
public class killController {

    private static final Logger logger = LoggerFactory.getLogger(killController.class);
    @Reference
    private IOrderService orderService;

    @Reference
    private IStockService stockService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/getOrder")
    public Order getOrder(){
        return orderService.selectOrder();
    }

    @GetMapping("/getStock")
    public Stock getStock(){
        return stockService.selectStock();
    }

    @RequestMapping("/killGoods/{sid}")
    @ResponseBody
    @Transactional
    @RateLimit(key = "killGoods", time = 1, count = 20)
    public void killGoods(@PathVariable int sid){
        Stock stock = checkStock(sid);
        decStock(stock);
        createOrder(stock);
        //统计接口历史访问量
        RedisAtomicInteger entityIdCounter = new RedisAtomicInteger("entityIdCounter", redisTemplate.getConnectionFactory());

        String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS");

        logger.info(date +" 累计访问次数：" + entityIdCounter.getAndIncrement());
    }

    /**
     * 检查库存
     * @param sid
     * @return
     */
    private Stock checkStock(int sid){
        Stock stock = stockService.selectStockById(sid);
        if (stock.getSale().equals(stock.getCount())){
            throw new RuntimeException("库存不足！");
        }
        return stock;
    }

    /**
     * 扣减库存
     * @param stock
     */
    private void decStock(Stock stock){
        int updateNum = stockService.updateStockById(stock);
        //只有不为0才能判断是成功执行了更新操作
        if (updateNum == 0){
            throw new RuntimeException("更新库存失败！");
        }
    }

    /**
     * 创建订单
     * @param stock
     */
    private void createOrder(Stock stock){
        Order order = new Order();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        orderService.createOrder(order);
    }

}
