package com.lkd.http.controller;

import com.lkd.entity.ChannelEntity;
import com.lkd.http.controller.vo.ChannelConfigReq;
import com.lkd.http.controller.vo.ChannelVO;
import com.lkd.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/channel")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    // 根据售货机编号查询货道列表
    @GetMapping("/channelList/{innerCode}")
    public List<ChannelEntity> channelList(@PathVariable("innerCode") String innerCode) {
        return channelService.channelList(innerCode);
    }

    // 货道设置
    @PutMapping("/channelConfig")
    public Boolean channelConfig(@RequestBody ChannelConfigReq channelConfigReq) {
        return channelService.channelConfig(channelConfigReq);
    }


    // 售货机商品是否还有库存
    @GetMapping("/hasCapacity/{innerCode}/{skuId}")
    public ChannelVO hasCapacity(@PathVariable(name = "innerCode") String innerCode,
                                 @PathVariable(name = "skuId") Long skuId){
        return channelService.hasCapacity(innerCode,skuId);
    }
}
