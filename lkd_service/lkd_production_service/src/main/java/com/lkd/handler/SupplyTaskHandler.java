package com.lkd.handler;

import cn.hutool.json.JSONUtil;
import com.lkd.business.MsgHandler;
import com.lkd.business.Topic;
import com.lkd.config.TopicConfig;
import com.lkd.contract.SupplyContract;
import com.lkd.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 自动捕获工单策略实现类
@Component
@Topic(TopicConfig.TASK_SUPPLY_TOPIC)
@Slf4j
public class SupplyTaskHandler implements MsgHandler {


    @Autowired
    private TaskService taskService;


    @Override
    public void process(String jsonMsg) {
        log.info("接收补货工单：{}",jsonMsg);
        SupplyContract supplyContract = JSONUtil.toBean(jsonMsg, SupplyContract.class);

        // 调用service处理
        taskService.autoSupplyTask(supplyContract);
    }
}
