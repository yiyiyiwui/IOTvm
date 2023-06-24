package com.lkd.http.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lkd.entity.TaskDetailsEntity;
import com.lkd.service.TaskDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/taskDetails")
public class TaskDetailController {

    @Autowired
    private TaskDetailsService taskDetailsService;

    // 查询补货详情
    @GetMapping("/{taskId}")
    public List<TaskDetailsEntity> getByTaskId(@PathVariable("taskId") Long taskId) {
        // 条件
        LambdaQueryWrapper<TaskDetailsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDetailsEntity::getTaskId, taskId);
        // 执行查询并返回
        return taskDetailsService.list(queryWrapper);
    }
}
