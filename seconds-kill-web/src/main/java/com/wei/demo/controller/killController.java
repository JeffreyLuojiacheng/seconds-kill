package com.wei.demo.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wei.demo.annotation.CountVisitNum;
import com.wei.demo.annotation.RateLimit;
import com.wei.demo.constant.RedisConstant;
import com.wei.demo.entity.Order;
import com.wei.demo.entity.Stock;
import com.wei.demo.service.IOrderService;
import com.wei.demo.service.IStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/getAllOrders")
    public List<Order> getAllOrders() {
        return orderService.selectAllOrders();
    }

    @GetMapping("/getAllStocks")
    public List<Stock> getAllStocks() {
        return stockService.selectAllStocks();
    }

    @GetMapping("/loadDataFromDBtoRedis")
    @CountVisitNum(key = "loadDataFromDBtoRedis")
    public void loadDataFromDBtoRedis(){
        List<Stock> stocks = stockService.selectAllStocks();
        if (null != stocks && stocks.size() != 0) {
            for (Stock stock : stocks) {
                int sid = stock.getId();
                redisTemplate.opsForValue().set(RedisConstant.STOCK_KEY_PREFIX + sid, JSON.toJSONString(stock));
            }
        }
    }

    @RequestMapping("/killGoods/{sid}")
    @ResponseBody
    @Transactional
    @RateLimit(key = "killGoods", time = 1, count = 50)
    public void killGoods(@PathVariable int sid) {
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
        String stockJson = redisTemplate.opsForValue().get(RedisConstant.STOCK_KEY_PREFIX + sid);
        Stock stock = JSON.parseObject(stockJson,new TypeReference<Stock>(){});
        if (stock.getCount().equals(stock.getSale())) {
            throw new RuntimeException("库存不足！");
        }
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
        stock.setSale(stock.getSale() + 1);
        stock.setVersion(stock.getVersion() + 1);
        redisTemplate.opsForValue().set(RedisConstant.STOCK_KEY_PREFIX + stock.getId(), JSON.toJSONString(stock));
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
