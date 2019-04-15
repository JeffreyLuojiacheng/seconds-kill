package com.wei.demo.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wei.demo.constant.RedisConstant;
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
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/getOrder")
    public Order getOrder() {
        return orderService.selectOrder();
    }

    @GetMapping("/getStock")
    public Stock getStock() {
        return stockService.selectStock();
    }

    @RequestMapping("/killGoods/{sid}")
    @ResponseBody
    @Transactional
    //@RateLimit(key = "killGoods", time = 1, count = 50)
    public void killGoods(@PathVariable int sid) {
        countVisitNum();
        //saleFromDB(sid);
        saleFromRedis(sid);
    }

    /**
     * 1.第一种方案，从数据库中查询库存并修改
     * @param sid
     */
    private void saleFromDB(int sid) {
        Stock stock = checkStockFromDB(sid);
        decStockfromDB(stock);
        createOrder(stock);
    }

    /**
     * 2.第二种方案，从redis查询库存并修改，同时也要利用数据库的乐观锁防止“超卖”现象，
     * 需要注意的是保证数据库和redis的数据一致
     * @param sid
     */
    private void saleFromRedis(int sid) {
        Stock stock = checkStockFromRedis(sid);
        decStockFromRedis(stock);
        createOrder(stock);
    }

    /**
     * 统计接口历史访问量
     */
    private void countVisitNum() {

        RedisAtomicInteger entityIdCounter = new RedisAtomicInteger("entityIdCounter", redisTemplate.getConnectionFactory());

        String date = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS");

        logger.info(date + " 累计访问次数：" + entityIdCounter.getAndIncrement());
    }

    /**
     * 从数据库中检查库存
     *
     * @param sid
     * @return
     */
    private Stock checkStockFromDB(int sid) {
        Stock stock = stockService.selectStockById(sid);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足！");
        }
        return stock;
    }

    /**
     * 从数据库中扣减库存
     *
     * @param stock
     */
    private void decStockfromDB(Stock stock) {
        int updateNum = stockService.updateStockById(stock);
        //只有不为0才能判断是成功执行了更新操作
        if (updateNum == 0) {
            throw new RuntimeException("更新库存失败！");
        }
    }

    /**
     * 从Redis中检查库存
     * @param sid
     * @return
     */
    private Stock checkStockFromRedis(int sid) {
        Integer count = Integer.parseInt(redisTemplate.opsForValue().get(RedisConstant.STOCK_COUNT_KEY_PREFIX + sid));
        Integer sale = Integer.parseInt(redisTemplate.opsForValue().get(RedisConstant.STOCK_SALE_KEY_PREFIX + sid));
        if (count.equals(sale)) {
            throw new RuntimeException("库存不足！");
        }
        String name = redisTemplate.opsForValue().get(RedisConstant.STOCK_NAME_KEY_PREFIX + sid);
        Integer version = Integer.parseInt(redisTemplate.opsForValue().get(RedisConstant.STOCK_VERSION_KEY_PREFIX + sid));
        Stock stock = new Stock();
        stock.setId(sid);
        stock.setName(name);
        stock.setCount(count);
        stock.setSale(sale);
        stock.setVersion(version);
        return stock;
    }

    /**
     * 从Redis中扣减库存
     * @param stock
     */
    private void decStockFromRedis(Stock stock) {
        int updateNum = stockService.updateStockById(stock);
        if (updateNum == 0) {
            throw new RuntimeException("更新库存失败！");
        }
        redisTemplate.opsForValue().increment(RedisConstant.STOCK_SALE_KEY_PREFIX + stock.getId(), 1);
        redisTemplate.opsForValue().increment(RedisConstant.STOCK_VERSION_KEY_PREFIX + stock.getId(), 1);
    }

    /**
     * 创建订单
     *
     * @param stock
     */
    private void createOrder(Stock stock) {
        Order order = new Order();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        orderService.createOrder(order);
    }

}
