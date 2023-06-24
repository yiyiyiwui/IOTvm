package com.lkd.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lkd.entity.TaskEntity;
import org.apache.ibatis.annotations.*;

public interface TaskDao extends BaseMapper<TaskEntity> {

    // 手动编写关联映射
    @Select("select * from tb_task where task_id=#{taskId}")
    @Results(id = "taskMap", value = {
            @Result(column = "task_id", property = "taskId",id = true),
            @Result(column = "product_type_id", property = "productTypeId"),
            @Result(column = "product_type_id", property = "taskType", one = @One(select = "com.lkd.dao.TaskTypeDao.selectById")),
            @Result(column = "task_status", property = "taskStatus"),
            @Result(column = "task_status", property = "taskStatusTypeEntity", one = @One(select = "com.lkd.dao.TaskStatusTypeDao.selectById"))
    })
    TaskEntity findById(Long taskId);
}
