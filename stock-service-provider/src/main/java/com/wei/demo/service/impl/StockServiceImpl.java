package com.wei.demo.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.wei.demo.entity.Stock;
import com.wei.demo.mapper.StockMapper;
import com.wei.demo.service.IStockService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@Service
public class StockServiceImpl implements IStockService {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public List<Stock> selectAllStocks() {
        return stockMapper.selectAllStocks();
    }

    @Override
    public Stock selectStockById(int sid) {
        return stockMapper.selectStockById(sid);
    }

    @Override
    public int updateStockById(Stock stock) {
        int updateNum = stockMapper.updateStockById(stock);
        return updateNum;
    }
}
