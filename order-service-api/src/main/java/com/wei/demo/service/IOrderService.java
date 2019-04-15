package com.wei.demo.service;

import com.wei.demo.entity.Order;

import java.util.List;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
public interface IOrderService {

    List<Order> selectAllOrders();

    void createOrder(Order order);
}
