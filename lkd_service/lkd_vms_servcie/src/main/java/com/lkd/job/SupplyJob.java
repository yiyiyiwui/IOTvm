package com.lkd.job;

import com.lkd.service.VendingMachineService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.ShardingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SupplyJob {


    @Autowired
    private VendingMachineService vendingMachineService;

    @XxlJob("supplyJobHandler")
    public ReturnT<String> supplyJobHandler(String param) throws Exception {
        log.info("自动补货任务开始");

        // 获取分片数据
        ShardingUtil.ShardingVO shardingVo = ShardingUtil.getShardingVo();
        log.info("总共片：{}，当前片索引：{}",shardingVo.getTotal(),shardingVo.getIndex());

        // 设备检测补货
        vendingMachineService.autoSupplyTask(shardingVo.getTotal(),shardingVo.getIndex());

        return ReturnT.SUCCESS;
    }
}