package com.lkd.http.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserWorkVO implements Serializable {

    /**
     * 用户名
     */
    private String userName;


    /**
     * 完成工单数
     */
    private Integer workCount;

}