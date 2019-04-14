package com.wei.demo.mapper;

import com.wei.demo.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@Mapper
public interface StockMapper {

    Stock selectStock();

    Stock selectStockById(@Param("id") int id);

    int updateStockById(@Param("stock")Stock stock);
}
