package com.wei.demo.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wei.demo.entity.Order;
import com.wei.demo.entity.Stock;
import com.wei.demo.service.IOrderService;
import com.wei.demo.service.IStockService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@RestController
@RequestMapping("/kill")
public class killController {

    @Reference
    private IOrderService orderService;

    @Reference
    private IStockService stockService;

    @GetMapping("/getOrder")
    public Order getOrder(){
        return orderService.selectOrder();
    }

    @GetMapping("/getStock")
    public Stock getStock(){
        return stockService.selectStock();
    }

    @RequestMapping("/createOrder/{sid}")
    @ResponseBody
    @Transactional
    public void createOrder(@PathVariable int sid){
        Stock stock = checkStock(sid);
        decStock(stock);
        createOrder(stock);
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
