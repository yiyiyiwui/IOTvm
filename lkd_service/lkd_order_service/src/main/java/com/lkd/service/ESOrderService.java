package com.lkd.service;

import com.lkd.http.controller.vo.OrderVO;
import com.lkd.http.controller.vo.Pager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ES 订单库业务逻辑接口
 */
public interface ESOrderService {


    // 订单查询
    Pager<OrderVO> search(Integer pageIndex, Integer pageSize, String orderNo, String openId, LocalDate startDate, LocalDate endDate) throws IOException;
}
