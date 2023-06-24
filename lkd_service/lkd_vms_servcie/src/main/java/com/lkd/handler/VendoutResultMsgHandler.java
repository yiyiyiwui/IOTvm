package com.lkd.handler;

import cn.hutool.json.JSONUtil;
import com.lkd.business.MsgHandler;
import com.lkd.business.Topic;
import com.lkd.config.TopicConfig;
import com.lkd.contract.VendoutResultContract;
import com.lkd.service.VendingMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Topic(TopicConfig.VMS_RESULT_TOPIC)
@Slf4j
public class VendoutResultMsgHandler implements MsgHandler {

    @Autowired
    private VendingMachineService vmService;

    @Override
    public void process(String jsonMsg)  {
      log.info("售货机微服务接收出货结果消息：{}",jsonMsg);

        VendoutResultContract vendoutResultContract = JSONUtil.toBean(jsonMsg, VendoutResultContract.class);

        // 调用service处理业务
        vmService.vendoutResult(vendoutResultContract);
    }
}