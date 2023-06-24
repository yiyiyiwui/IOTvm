package com.lkd.handler;

import cn.hutool.json.JSONUtil;
import com.lkd.business.MsgHandler;
import com.lkd.business.Topic;
import com.lkd.config.TopicConfig;
import com.lkd.contract.TaskCompleteContract;
import com.lkd.service.VendingMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 运维工单策略实现
@Topic(TopicConfig.VMS_COMPLETED_TOPIC)
@Component
public class TaskCompletedMsgHandler implements MsgHandler {

    @Autowired
    private VendingMachineService vendingMachineService;

    // 接收消息内容，调用service更新设备状态
    @Override
    public void process(String jsonMsg) {

        TaskCompleteContract taskCompleteContract = JSONUtil.toBean(jsonMsg, TaskCompleteContract.class);

        vendingMachineService.updateVMStatus(taskCompleteContract);
    }
}
