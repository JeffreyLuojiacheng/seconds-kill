package com.wei.demo.mapper;

import com.wei.demo.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@Mapper
public interface StockMapper {

    List<Stock> selectAllStocks();

    Stock selectStockById(@Param("id") int id);

    int updateStockById(@Param("stock")Stock stock);
}
