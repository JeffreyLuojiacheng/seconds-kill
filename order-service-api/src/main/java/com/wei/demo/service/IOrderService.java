package com.wei.demo.service;

import com.wei.demo.entity.Order;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
public interface IOrderService {

    Order selectOrder();

    void createOrder(Order order);
}
