package com.lkd.http.controller;

import com.lkd.feign.OrderServiceFeignClient;
import com.lkd.http.controller.vo.PayVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderServiceFeignClient orderServiceFeignClient;

    // 创建订单，生成支付二维码
    @PostMapping("/requestPay")
    public Map<String, String> requestPay(@RequestBody PayVO payVO) {
        return orderServiceFeignClient.weixinPay(payVO);
    }
}