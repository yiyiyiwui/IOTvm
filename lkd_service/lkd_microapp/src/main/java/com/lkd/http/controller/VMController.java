package com.lkd.http.controller;

import com.lkd.feign.VmServiceFeignClient;
import com.lkd.http.controller.vo.SkuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vm")
public class VMController {

    @Autowired
    private VmServiceFeignClient vmServiceFeignClient;

    // 查询指定售货机的商品列表
    @GetMapping("/skuList/{innerCode}")
    public List<SkuVO> skuList(@PathVariable("innerCode") String innerCode) {

        // feign远程调用
        return vmServiceFeignClient.getSkuListByInnerCode(innerCode);
    }

}