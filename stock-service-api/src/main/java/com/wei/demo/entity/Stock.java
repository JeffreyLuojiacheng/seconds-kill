package com.wei.demo.entity;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@Getter
@Setter
public class Stock implements Serializable{
    private Integer id;

    private String name;

    private Integer count;

    private Integer sale;

    private Integer version;
}
