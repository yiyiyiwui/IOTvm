package com.lkd.feign;

import com.lkd.http.controller.vo.ChannelVO;
import com.lkd.http.controller.vo.SkuVO;
import com.lkd.http.controller.vo.VmVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("vm-service")
public interface VmServiceFeignClient {


    // 根据设备号查询VmVo
    @GetMapping("/vm/getInnerCode/{innerCode}")
    public VmVO getInnerCode(@PathVariable("innerCode") String innerCode);


    // 获取售货机商品列表
    @GetMapping("/vm/skuList/{innerCode}")
    public List<SkuVO> getSkuListByInnerCode(@PathVariable("innerCode") String innerCode);


    // 售货机商品是否还有库存
    @GetMapping("/channel/hasCapacity/{innerCode}/{skuId}")
    public ChannelVO hasCapacity(@PathVariable(name = "innerCode") String innerCode,
                                 @PathVariable(name = "skuId") Long skuId);
}
