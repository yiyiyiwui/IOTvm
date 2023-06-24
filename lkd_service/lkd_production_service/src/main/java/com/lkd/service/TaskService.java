package com.lkd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkd.contract.SupplyContract;
import com.lkd.contract.VmStatusContract;
import com.lkd.entity.TaskEntity;
import com.lkd.entity.TaskStatusTypeEntity;
import com.lkd.http.vo.CancelTaskViewModel;
import com.lkd.http.vo.TaskCollectVO;
import com.lkd.http.vo.TaskReportInfoVO;
import com.lkd.http.vo.TaskViewModel;
import com.lkd.http.controller.vo.Pager;
import com.lkd.http.controller.vo.UserWorkVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单业务逻辑
 */
public interface TaskService extends IService<TaskEntity> {


    //  查询所有工单状态
    List<TaskStatusTypeEntity> getAllStatus();


    // 搜索工单
    Pager<TaskEntity> search(Long pageIndex, Long pageSize, String innerCode, Integer userId, String taskCode, Integer status, Boolean isRepair, String start, String end);



    // 获取人员排名
    List<UserWorkVO> getUserWorkTop10(LocalDate startTime, LocalDate endTime, Boolean isRepair, Long regionId);


    /**
     * 获取工单报表
     * @param start
     * @param end
     * @return
     */
    List<TaskCollectVO> getTaskReport(LocalDate start, LocalDate end);

    /**
     * 获取用户工作量详情
     * @param userId
     * @param start
     * @param end
     * @return
     */
    UserWorkVO getUserWork(Integer userId, LocalDateTime start, LocalDateTime end);

    // 创建工单
    Boolean create(TaskViewModel taskViewModel);

    // 接受工单
    Boolean accept(Long taskId,Integer userId);

    // 拒绝/取消工单
    Boolean cancel(Long taskId, CancelTaskViewModel cancelTaskViewModel, Integer userId);

    // 完成工单
    Boolean complete(Long taskId, Integer userId);


    // 当日工单统计
    List<TaskReportInfoVO> taskReportInfo(LocalDateTime start, LocalDateTime end);


    // 获取同一天内分配的工单最少的人
    Integer getLeastUser(Long regionId,Boolean isRepair);


    // 处理自动维修工单
    void autoRepairTask(VmStatusContract statusContract);

    // 处理自动补货工单
    void autoSupplyTask(SupplyContract supplyContract);
}
