package com.wei.demo.mapper;

import com.wei.demo.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@Mapper
public interface OrderMapper {

    List<Order> selectAllOrders();

    void createOrder(@Param("order") Order order);
}
