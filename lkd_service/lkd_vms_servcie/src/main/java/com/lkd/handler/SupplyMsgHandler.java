package com.lkd.handler;

import cn.hutool.json.JSONUtil;
import com.lkd.business.MsgHandler;
import com.lkd.business.Topic;
import com.lkd.config.TopicConfig;
import com.lkd.contract.SupplyContract;
import com.lkd.service.VendingMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 补货策略实现类
@Component
@Topic(TopicConfig.VMS_SUPPLY_TOPIC)
public class SupplyMsgHandler implements MsgHandler {


    @Autowired
    private VendingMachineService vendingMachineService;


    // 完成补货业务
    @Override
    public void process(String jsonMsg) {
        // json转对象
        SupplyContract supplyContract = JSONUtil.toBean(jsonMsg, SupplyContract.class);
        // 调用service补货
        vendingMachineService.supply(supplyContract);

    }
}
