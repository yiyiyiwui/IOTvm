package com.lkd.handler;

import cn.hutool.json.JSONUtil;
import com.lkd.business.MsgHandler;
import com.lkd.business.Topic;
import com.lkd.config.TopicConfig;
import com.lkd.contract.VendoutContract;
import com.lkd.service.VendingMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Topic(TopicConfig.VMS_VENDOUT_TOPIC)
public class VendoutMsgHandler implements MsgHandler {

    @Autowired
    private VendingMachineService vmService;

    @Override
    public void process(String jsonMsg) {
        log.info("售货机微服务接受订单消息：{}", jsonMsg);
        VendoutContract vendoutContract = JSONUtil.toBean(jsonMsg, VendoutContract.class);

        // 调用service完成出货
        Boolean isSuccess = vmService.vendout(vendoutContract);

        if (isSuccess == false) {
            // TODO 执行退款逻辑
        }
    }
}