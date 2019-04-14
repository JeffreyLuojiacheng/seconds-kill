package com.wei.demo.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author weiwenfeng
 * @date 2019/4/14
 */
@Getter
@Setter
public class Order implements Serializable {

    private Integer id;

    private String name;

    private Integer sid;

    private Date createTime;
}
