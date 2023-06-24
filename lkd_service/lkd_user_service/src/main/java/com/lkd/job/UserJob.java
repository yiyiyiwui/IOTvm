package com.lkd.job;

import com.lkd.common.VMSystem;
import com.lkd.entity.UserEntity;
import com.lkd.service.UserService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class UserJob {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @XxlJob("workCountInitJobHandler")
    public ReturnT<String> workCountInitJobHandler(String param) {
        log.info("每日工单量列表初始化");

        // 1.查询运维、运营人员的列表信息
        List<UserEntity> userList = userService.lambdaQuery()
                .ne(UserEntity::getRoleCode, VMSystem.USER_ROLE_ADMIN_ODE)
                .list();

        // 2.存入到redis中
        for (UserEntity user : userList) {
            // 2-1.生成redis中key
            String key = VMSystem.generateKey(user.getRegionId(), user.getRoleCode());
            // 2-2.存入redis
            redisTemplate.opsForZSet().add(key, user.getId().toString(), 0);
            // 2-3.设置有效期为1天
            redisTemplate.expire(key, Duration.ofDays(1));
        }

        return ReturnT.SUCCESS;
    }
}