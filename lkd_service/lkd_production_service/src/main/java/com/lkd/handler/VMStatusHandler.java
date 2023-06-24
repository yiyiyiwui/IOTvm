package com.lkd.handler;

import cn.hutool.json.JSONUtil;
import com.lkd.business.MsgHandler;
import com.lkd.business.Topic;
import com.lkd.common.VMSystem;
import com.lkd.config.TopicConfig;
import com.lkd.contract.VmStatusContract;
import com.lkd.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 运维工单，策略模式实现类
@Component
@Topic(TopicConfig.VMS_STATUS_TOPIC)
@Slf4j
public class VMStatusHandler implements MsgHandler {

    @Autowired
    private TaskService taskService;

    @Override
    public void process(String jsonMsg) {
        log.info("接收设备状态信息：{}", jsonMsg);
        // json转java
        VmStatusContract vmStatusContract = JSONUtil.toBean(jsonMsg, VmStatusContract.class);
        // 调用自动维修工单创建方法
        taskService.autoRepairTask(vmStatusContract);
    }
}
