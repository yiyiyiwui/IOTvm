package com.lkd.feign;

import com.lkd.http.controller.vo.PayVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "order-service")
public interface OrderServiceFeignClient {


    //创建订单
    @PostMapping("/order/weixinPay")
    public Map<String, String> weixinPay(@RequestBody PayVO payVO);
}
