package com.wei.demo.mapper;

import com.wei.demo.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@Mapper
public interface OrderMapper {

    Order selectOrder();

    void createOrder(@Param("order") Order order);
}
