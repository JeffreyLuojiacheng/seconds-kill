package com.wei.demo.service;

import com.wei.demo.entity.Stock;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
public interface IStockService {

    Stock selectStock();

    Stock selectStockById(int sid);

    int updateStockById(Stock stock);
}
