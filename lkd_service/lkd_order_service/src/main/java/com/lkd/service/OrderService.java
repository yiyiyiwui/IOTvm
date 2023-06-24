package com.lkd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkd.contract.OrderCheck;
import com.lkd.contract.VendoutResultContract;
import com.lkd.entity.OrderEntity;
import com.lkd.http.controller.vo.PayVO;

import java.util.Map;

public interface OrderService extends IService<OrderEntity> {


    //创建订单
    Map<String, String> weixinPay(PayVO payVO);


    // 处理超时订单
    void handlerTimeOutOrder(OrderCheck orderCheck);

    // 支付成功，修改订单状态
    void payNotify(Map<String, String> resultMap);


    // 订单微服务处理出货结果
    Boolean vendoutResult(VendoutResultContract vendoutContract);
}
