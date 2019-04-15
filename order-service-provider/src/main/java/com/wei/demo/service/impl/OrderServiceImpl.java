package com.wei.demo.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.wei.demo.entity.Order;
import com.wei.demo.mapper.OrderMapper;
import com.wei.demo.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<Order> selectAllOrders() {
        return orderMapper.selectAllOrders();
    }

    @Override
    public void createOrder(Order order) {
        orderMapper.createOrder(order);
    }
}
