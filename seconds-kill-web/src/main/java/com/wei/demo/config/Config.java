package com.wei.demo.config;

import com.wei.demo.entity.Stock;
import com.wei.demo.service.IStockService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author weiwenfeng
 * @date 2019/4/15
 */
@Configuration
public class Config {

    @Reference
    private IStockService stockService;

    @Bean
    public void init(){
        List<Stock> stocks = stockService.selectAllStocks();
        System.out.println();
    }
}
