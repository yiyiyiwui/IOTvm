package com.lkd.http.controller;

import com.lkd.entity.TaskEntity;
import com.lkd.entity.TaskStatusTypeEntity;
import com.lkd.http.controller.vo.Pager;
import com.lkd.http.controller.vo.UserWorkVO;
import com.lkd.http.vo.CancelTaskViewModel;
import com.lkd.http.vo.TaskCollectVO;
import com.lkd.http.vo.TaskReportInfoVO;
import com.lkd.http.vo.TaskViewModel;
import com.lkd.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController extends BaseController {
    @Autowired
    private TaskService taskService;

    /**
     * 查询所有工单状态
     *
     * @return
     */
    @GetMapping("/allTaskStatus")
    public List<TaskStatusTypeEntity> getAllStatus() {
        return taskService.getAllStatus();
    }

    /**
     * 搜索工单
     *
     * @param pageIndex
     * @param pageSize
     * @param innerCode 设备编号
     * @param userId    工单所属人Id
     * @param taskCode  工单编号
     * @param status    工单状态
     * @param isRepair  是否是维修工单
     * @return
     */
    @GetMapping("/search")
    public Pager<TaskEntity> search(
            @RequestParam(value = "pageIndex", required = false, defaultValue = "1") Long pageIndex,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize,
            @RequestParam(value = "innerCode", required = false, defaultValue = "") String innerCode,
            @RequestParam(value = "userId", required = false, defaultValue = "") Integer userId,
            @RequestParam(value = "taskCode", required = false, defaultValue = "") String taskCode,
            @RequestParam(value = "status", required = false, defaultValue = "") Integer status,
            @RequestParam(value = "isRepair", required = false, defaultValue = "") Boolean isRepair,
            @RequestParam(value = "start", required = false, defaultValue = "") String start,
            @RequestParam(value = "end", required = false, defaultValue = "") String end) {
        return taskService.search(pageIndex, pageSize, innerCode, userId, taskCode, status, isRepair, start, end);
    }

    /**
     * 获取人员排名
     *
     * @param start
     * @param end
     * @param isRepair
     * @return
     */
    @GetMapping("/userWorkTop10/{start}/{end}/{isRepair}/{regionId}")
    public List<UserWorkVO> getUserWorkTop10(@PathVariable String start,
                                             @PathVariable String end,
                                             @PathVariable Boolean isRepair,
                                             @PathVariable Long regionId) {
        return taskService.getUserWorkTop10(
                LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE),
                LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE),
                isRepair,
                regionId);
    }

    /**
     * 获取工单报表
     *
     * @param start
     * @param end
     * @return
     */
    @GetMapping("/collectReport/{start}/{end}")
    public List<TaskCollectVO> getTaskCollectReport(@PathVariable String start,
                                                    @PathVariable String end) {
        return taskService.getTaskReport(
                LocalDate.parse(start, DateTimeFormatter.ISO_LOCAL_DATE),
                LocalDate.parse(end, DateTimeFormatter.ISO_LOCAL_DATE));
    }

    /**
     * 获取用户工作量详情
     *
     * @param userId
     * @param start
     * @param end
     * @return
     */
    @GetMapping("/userWork")
    public UserWorkVO getUserWork(@RequestParam Integer userId,
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return taskService.getUserWork(userId, start, end);
    }

    // 工单详情
    @GetMapping("/taskInfo/{taskId}")
    public TaskEntity taskInfo(@PathVariable("taskId") Long taskId) {
        return taskService.getById(taskId);
    }

    // 创建工单
    @PostMapping("/create")
    public Boolean create(@RequestBody TaskViewModel taskViewModel) {
        // 获取登录人id
        Integer assignorId = getUserId();
        taskViewModel.setAssignorId(assignorId);

        return taskService.create(taskViewModel);
    }

    // 接受工单
    @GetMapping("/accept/{taskId}")
    public Boolean accept(@PathVariable("taskId") Long taskId) {
        // 获取登录人id
        Integer userId = getUserId();
        return taskService.accept(taskId, userId);
    }

    // 拒绝/取消工单
    @PostMapping("/cancel/{taskId}")
    public Boolean cancel(@PathVariable("taskId") Long taskId, @RequestBody CancelTaskViewModel cancelTaskViewModel) {
        // 获取登录人id
        Integer userId = getUserId();
        return taskService.cancel(taskId, cancelTaskViewModel, userId);
    }

    // 完成工单
    @GetMapping("/complete/{taskId}")
    public Boolean complete(@PathVariable("taskId") Long taskId) {
        // 获取登录人id
        Integer userId = getUserId();
        return taskService.complete(taskId, userId);
    }

    // 当日工单统计
    @GetMapping("/taskReportInfo/{start}/{end}")
    public List<TaskReportInfoVO> taskReportInfo(
            @PathVariable("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @PathVariable("end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return taskService.taskReportInfo(start, end);
    }
}