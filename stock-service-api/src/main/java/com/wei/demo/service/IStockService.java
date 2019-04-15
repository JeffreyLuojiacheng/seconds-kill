package com.wei.demo.service;

import com.wei.demo.entity.Stock;

import java.util.List;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
public interface IStockService {

    List<Stock> selectAllStocks();

    Stock selectStockById(int sid);

    int updateStockById(Stock stock);
}
