package com.lkd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.lkd.common.VMSystem;
import com.lkd.config.TopicConfig;
import com.lkd.contract.*;
import com.lkd.dao.TaskDao;
import com.lkd.emq.MqttProducer;
import com.lkd.entity.TaskDetailsEntity;
import com.lkd.entity.TaskEntity;
import com.lkd.entity.TaskStatusTypeEntity;
import com.lkd.exception.LogicException;
import com.lkd.feign.UserServiceFeignClient;
import com.lkd.feign.VmServiceFeignClient;
import com.lkd.http.controller.vo.Pager;
import com.lkd.http.controller.vo.UserVO;
import com.lkd.http.controller.vo.UserWorkVO;
import com.lkd.http.controller.vo.VmVO;
import com.lkd.http.vo.*;
import com.lkd.service.TaskDetailsService;
import com.lkd.service.TaskService;
import com.lkd.service.TaskStatusTypeService;
import com.lkd.service.TaskTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.javassist.expr.NewArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskServiceImpl extends ServiceImpl<TaskDao, TaskEntity> implements TaskService {

    @Autowired
    private TaskStatusTypeService taskStatusTypeService;

    @Autowired
    private TaskTypeService taskTypeService;
    @Autowired
    private UserServiceFeignClient userServiceFeignClient;
    @Autowired
    private TaskDetailsService taskDetailsService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 查询所有工单状态
    @Override
    public List<TaskStatusTypeEntity> getAllStatus() {
        return taskStatusTypeService.list();
    }

    // 搜索工单
    @Override
    public Pager<TaskEntity> search(Long pageIndex, Long pageSize, String innerCode, Integer userId, String taskCode, Integer status, Boolean isRepair, String start, String end) {
        // 1.分页
        Page<TaskEntity> page = new Page<>(pageIndex, pageSize);
        // 2.条件
        LambdaQueryWrapper<TaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StrUtil.isNotEmpty(innerCode), TaskEntity::getInnerCode, innerCode) // 设备号
                .eq(ObjectUtil.isNotEmpty(userId), TaskEntity::getUserId, userId) // 干活人
                .eq(StrUtil.isNotEmpty(taskCode), TaskEntity::getTaskCode, taskCode) // 工单号
                .eq(ObjectUtil.isNotEmpty(status), TaskEntity::getTaskStatus, status); // 工单状态

        if (ObjectUtil.isNotEmpty(isRepair)) {// 是否为运维工单
            if (isRepair) { // 运维
                queryWrapper.ne(TaskEntity::getProductTypeId, VMSystem.TASK_TYPE_SUPPLY);
            } else { // 运营
                queryWrapper.eq(TaskEntity::getProductTypeId, VMSystem.TASK_TYPE_SUPPLY);
            }
        }
        // 对时间进行处理
        if (StrUtil.isNotEmpty(start) && StrUtil.isNotEmpty(end)) {
            LocalDateTime minTime = LocalDate.parse(start).atTime(LocalTime.MIN);
            LocalDateTime maxTime = LocalDate.parse(end).atTime(LocalTime.MAX);
            queryWrapper.between(TaskEntity::getUpdateTime, minTime, maxTime);
        }

        // 排序
        queryWrapper.orderByDesc(TaskEntity::getUpdateTime);

        // 3.查询并返回分页对象
        this.page(page, queryWrapper);

        return Pager.build(page);
    }

    // 获取人员排名
    @Override
    public List<UserWorkVO> getUserWorkTop10(LocalDate startTime, LocalDate endTime, Boolean isRepair, Long regionId) {
        // select
        //     user_name,
        //     count(*) user_id
        // from
        //     tb_task
        // where
        //     task_status = 4
        //     and
        //     update_time >= '2022-12-01 00:00:00'
        //     and
        //     update_time < '2023-01-01 00:00:00'
        //     and
        //     region_id = ?
        //     and
        //     product_type_id = ?
        // group by user_name
        // order by num desc
        // limit 10

        QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<TaskEntity>();
        queryWrapper.select(" user_name,count(*) user_id ");
        queryWrapper.last("order by user_id desc  limit 10");
        queryWrapper.lambda()
                .eq(TaskEntity::getTaskStatus, VMSystem.TASK_STATUS_FINISH)
                .ge(TaskEntity::getUpdateTime, startTime)
                .lt(TaskEntity::getUpdateTime, endTime.plusDays(1))
                .groupBy(TaskEntity::getUserName);

        if (regionId > 0) {
            queryWrapper.lambda().eq(TaskEntity::getRegionId, regionId);
        }
        if (isRepair) {
            queryWrapper.lambda().ne(TaskEntity::getProductTypeId, VMSystem.TASK_TYPE_SUPPLY);
        } else {
            queryWrapper.lambda().eq(TaskEntity::getProductTypeId, VMSystem.TASK_TYPE_SUPPLY);
        }

        List<TaskEntity> resultList = this.list(queryWrapper);

        // 将 List<TaskEntity> 变为 List<UserWorkVO>
        List<UserWorkVO> userWorks = resultList.stream().map(taskEntity -> {
            var userWorkVO = new UserWorkVO();
            userWorkVO.setUserName(taskEntity.getUserName());
            userWorkVO.setWorkCount(taskEntity.getUserId());
            return userWorkVO;
        }).collect(Collectors.toList());

        return userWorks;
    }

    /**
     * 获取工单报表
     *
     * @param start
     * @param end
     * @return
     */
    @Override
    public List<TaskCollectVO> getTaskReport(LocalDate start, LocalDate end) {
        List<TaskCollectVO> taskCollectVOList = Lists.newArrayList();
        //从开始日期到截至日期，逐条统计
        start.datesUntil(end.plusDays(1), Period.ofDays(1))
                .forEach(date -> {
                    var taskCollectVO = new TaskCollectVO();
                    taskCollectVO.setCollectDate(date);
                    taskCollectVO.setProgressCount(count(date, VMSystem.TASK_STATUS_PROGRESS)); //进行中工单数
                    taskCollectVO.setFinishCount(count(date, VMSystem.TASK_STATUS_FINISH));//完成工单数
                    taskCollectVO.setCancelCount(count(date, VMSystem.TASK_STATUS_CANCEL));//取消工单数
                    taskCollectVOList.add(taskCollectVO);
                });
        return taskCollectVOList;
    }

    /**
     * 获取用户工作量详情
     *
     * @param userId
     * @param start
     * @param end
     * @return
     */
    @Override
    public UserWorkVO getUserWork(Integer userId, LocalDateTime start, LocalDateTime end) {
        var userWork = new UserWorkVO();
        userWork.setUserId(userId);

        //获取用户完成工单数
        var workCount = this.getCountByUserId(userId, VMSystem.TASK_STATUS_FINISH, start.toLocalDate(), end);
        userWork.setWorkCount(workCount);

        //获取工单总数
        var total = this.getCountByUserId(userId, null, start.toLocalDate(), end);
        userWork.setTotal(total);

        //获取用户拒绝工单数
        var cancelCount = this.getCountByUserId(userId, VMSystem.TASK_STATUS_CANCEL, start.toLocalDate(), end);
        userWork.setCancelCount(cancelCount);

        //获取进行中得工单数
        var progressTotal = this.getCountByUserId(userId, VMSystem.TASK_STATUS_PROGRESS, start.toLocalDate(), end);
        userWork.setProgressTotal(progressTotal);

        return userWork;
    }

    @Autowired
    private VmServiceFeignClient vmServiceFeignClient;

    // 创建工单
    @Transactional
    @Override
    public Boolean create(TaskViewModel taskViewModel) {
        // 1.参数校验
        verifyTaskMsg(taskViewModel);
        // 2.校验售货机是否存在
        VmVO vm = vmServiceFeignClient.getInnerCode(taskViewModel.getInnerCode());
        if (ObjectUtil.isEmpty(vm)) {
            throw new LogicException("售货机不存在");
        }
        // 3.校验售货机状态与工单类型是否相符
        checkCreateTask(vm.getVmStatus(), taskViewModel.getProductType());
        // 4.校验该设备是否有未完成同类型工单，如果有不能创建新工单
        if (hasTask(taskViewModel.getInnerCode(), taskViewModel.getProductType())) {
            // throw new LogicException("该设备下有未完成同类型工单，请完成后，再布置任务");
            log.info("该设备下有未完成同类型工单，请完成后，再布置任务");
            return false;
        }
        // 5.查询是否有该员工
        UserVO user = userServiceFeignClient.findById(taskViewModel.getUserId());
        if (ObjectUtil.isEmpty(user)) {
            throw new LogicException("该员工不存在");
        }
        // 6.校验非同区域下工作人员不能接受工单
        if (!vm.getRegionId().equals(user.getRegionId())) {
            throw new LogicException("非同区域下工作人员不能接受工单");
        }
        // 7.保存工单
        // 7-1 填充数据
        TaskEntity taskEntity = BeanUtil.copyProperties(taskViewModel, TaskEntity.class);
        taskEntity.setTaskCode(generateTaskCode()); // 工单号
        taskEntity.setTaskStatus(VMSystem.TASK_STATUS_CREATE); // 工单状态（待处理）
        taskEntity.setRegionId(vm.getRegionId()); // 区域id
        taskEntity.setUserName(user.getUserName()); // 员工名
        taskEntity.setDescript(taskViewModel.getDesc()); // 描述
        taskEntity.setProductTypeId(taskViewModel.getProductType()); // 工单类型id
        taskEntity.setAddr(vm.getNodeAddr()); // 点位地址
        // 7-2保存
        this.save(taskEntity);

        // 8.如果是补货工单，同时保存明细（详情）
        if (taskViewModel.getProductType() == VMSystem.TASK_TYPE_SUPPLY) {
            taskViewModel.getDetails().forEach(dto -> {
                // dto转实体
                TaskDetailsEntity taskDetails = BeanUtil.copyProperties(dto, TaskDetailsEntity.class);
                taskDetails.setTaskId(taskEntity.getTaskId()); // 工单id
                // 保存
                taskDetailsService.save(taskDetails);
            });
        }

        // 9.此员工工单量+1
        updateTaskZSet(taskEntity, 1);
        return true;
    }

    // 接受工单
    @Override
    public Boolean accept(Long taskId, Integer userId) {
        // 1.校验工单
        TaskEntity task = this.getById(taskId);
        if (ObjectUtil.isEmpty(task)) {
            throw new LogicException("工单不存在");
        }
        // 2 .校验工单状态
        if (!task.getTaskStatus().equals(VMSystem.TASK_STATUS_CREATE)) {
            throw new LogicException("工单状态必须为待处理");
        }
        // 3.当前登录人是否有权接受
        if (!task.getUserId().equals(userId)) {
            throw new LogicException("这不是你干的活");
        }
        // 4.更新订单状态
        task.setTaskStatus(VMSystem.TASK_STATUS_PROGRESS);
        return this.updateById(task);
    }

    // 拒绝/取消工单
    @Override
    public Boolean cancel(Long taskId, CancelTaskViewModel cancelTaskViewModel, Integer userId) {
        // 1.校验工单
        TaskEntity task = this.getById(taskId);
        if (ObjectUtil.isEmpty(task)) {
            throw new LogicException("工单不存在");
        }
        // 2 .校验工单状态
        if (task.getTaskStatus().equals(VMSystem.TASK_STATUS_CANCEL) || task.getTaskStatus().equals(VMSystem.TASK_STATUS_FINISH)) {
            throw new LogicException("工单不能取消或拒绝");
        }
        // 3.当前登录人是否有权取消
        if (!task.getUserId().equals(userId)) {
            throw new LogicException("这不是你干的活");
        }
        // 4.更新工单状态
        task.setTaskStatus(VMSystem.TASK_STATUS_CANCEL);
        task.setDescript(cancelTaskViewModel.getDesc());
        this.updateById(task);

        // 5.此员工工单量-1
        updateTaskZSet(task, -1);
        return true;
    }

    // 完成工单
    @Override
    public Boolean complete(Long taskId, Integer userId) {
        // 1.校验工单
        TaskEntity task = this.getById(taskId);
        if (ObjectUtil.isEmpty(task)) {
            throw new LogicException("工单不存在");
        }
        // 2 .校验工单状态
        if (task.getTaskStatus().equals(VMSystem.TASK_STATUS_CANCEL) || task.getTaskStatus().equals(VMSystem.TASK_STATUS_FINISH)) {
            throw new LogicException("工单不能取消或拒绝");
        }
        // 3.当前登录人是否有权取消
        if (!task.getUserId().equals(userId)) {
            throw new LogicException("这不是你干的活");
        }
        // 4.更新工单状态
        task.setTaskStatus(VMSystem.TASK_STATUS_FINISH);
        boolean isSuccess = this.updateById(task);

        // 5. 发送运维工单：投放、撤机
        if (
                task.getProductTypeId().equals(VMSystem.TASK_TYPE_DEPLOY) ||
                        task.getProductTypeId().equals(VMSystem.TASK_TYPE_REVOKE)
        ) {
            // 发送mq消息
            sendRepairMsg(task);
        }

        // 6. 发送补货工单
        if (
                task.getProductTypeId().equals(VMSystem.TASK_TYPE_SUPPLY)
        ) {
            // 发送mq消息
            sendSupplyMsg(task);
        }

        return isSuccess;

    }

    // 抽取一个发送补货工单消息方法
    private void sendSupplyMsg(TaskEntity task) {
        // 封装发送消息内容
        SupplyContract supplyContract = new SupplyContract();
        supplyContract.setInnerCode(task.getInnerCode());// 售货机编号
        // 查询工单详情
        LambdaQueryWrapper<TaskDetailsEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(TaskDetailsEntity::getTaskId, task.getTaskId());
        List<TaskDetailsEntity> detailList = taskDetailsService.list(qw);
        // 封装补货数据
        List<SupplyChannel> supplyData = new ArrayList<>();
        for (TaskDetailsEntity taskDetailsEntity : detailList) {
            SupplyChannel supplyChannel = new SupplyChannel();
            supplyChannel.setChannelId(taskDetailsEntity.getChannelCode()); // 货道编号
            supplyChannel.setCapacity(taskDetailsEntity.getExpectCapacity());// 补货数量
            // 添加到集合中
            supplyData.add(supplyChannel);
        }
        supplyContract.setSupplyData(supplyData);// 补货数据

        // 发送mq消息
        String json = JSONUtil.toJsonStr(supplyContract);
        producer.send(TopicConfig.VMS_SUPPLY_TOPIC, json);

    }

    @Autowired
    private MqttProducer producer;

    // 抽取一个发送运维工单消息方法
    private void sendRepairMsg(TaskEntity task) {
        // 封装发送内容对象
        TaskCompleteContract taskCompleteContract = new TaskCompleteContract();
        taskCompleteContract.setInnerCode(task.getInnerCode()); // 售货机编号
        taskCompleteContract.setTaskType(task.getProductTypeId()); // 工单类型
        // 消息内容转json
        String json = JSONUtil.toJsonStr(taskCompleteContract);
        // 调用mq工具类发送消息
        producer.send(TopicConfig.VMS_COMPLETED_TOPIC, json);
    }

    // 当日工单统计
    @Override
    public List<TaskReportInfoVO> taskReportInfo(LocalDateTime start, LocalDateTime end) {
        // 1.查询运营数据
        // 1-1 工单总数
        Integer supplyCount = this.taskCount(start, end, false, null);
        // 1-2 完成数量
        Integer supplyFinshCount = this.taskCount(start, end, false, VMSystem.TASK_STATUS_FINISH);
        // 1-3 拒绝数量
        Integer supplyCancelCount = this.taskCount(start, end, false, VMSystem.TASK_STATUS_CANCEL);
        // 1-4 进行中数量
        Integer supplyProgeressCount = this.taskCount(start, end, false, VMSystem.TASK_STATUS_PROGRESS);
        // 1-4 运营人员
        Integer operatorCount = userServiceFeignClient.getOperatorCount();
        // 1-5 封装数据
        TaskReportInfoVO supplyVo = new TaskReportInfoVO();
        supplyVo.setTotal(supplyCount);
        supplyVo.setCompletedTotal(supplyFinshCount);
        supplyVo.setCancelTotal(supplyCancelCount);
        supplyVo.setWorkerCount(operatorCount);
        supplyVo.setProgressTotal(supplyProgeressCount);
        supplyVo.setDate(LocalDate.now().toString());

        // 2.查询运维数据
        // 2-1 工单总数
        Integer repairCount = this.taskCount(start, end, true, null);
        // 2-2 完成数量
        Integer repairFinishCount = this.taskCount(start, end, true, VMSystem.TASK_STATUS_FINISH);
        // 2-3 拒绝数量
        Integer repairCancelCount = this.taskCount(start, end, true, VMSystem.TASK_STATUS_CANCEL);
        // 2-4 进行中数量
        Integer repairProgeressCount = this.taskCount(start, end, true, VMSystem.TASK_STATUS_PROGRESS);
        // 2-5 运维人员
        Integer workerCount = userServiceFeignClient.getRepairerCount();
        TaskReportInfoVO repairVo = new TaskReportInfoVO();
        repairVo.setTotal(repairCount);
        repairVo.setCompletedTotal(repairFinishCount);
        repairVo.setCancelTotal(repairCancelCount);
        repairVo.setProgressTotal(repairProgeressCount);
        repairVo.setWorkerCount(workerCount);
        repairVo.setRepair(true);
        repairVo.setDate(LocalDate.now().toString());
        // 3.封装数据并返回
        ArrayList<TaskReportInfoVO> voList = new ArrayList<>();
        voList.add(supplyVo);
        voList.add(repairVo);
        return voList;
    }

    // 获取同一天内分配的工单最少的人
    @Override
    public Integer getLeastUser(Long regionId, Boolean isRepair) {
        // 1.确定是运维还是运营角色
        String roleCode = VMSystem.USER_ROLE_SUPPLY_ODE; // 默认运营角色
        if (isRepair == true) {
            roleCode = VMSystem.USER_ROLE_REPAIRER_CODE; // 运维角色
        }
        // 2.获取redis中key
        String key = VMSystem.generateKey(regionId, roleCode);
        // 3.查询zset（升序）
        Set<String> userSet = redisTemplate.opsForZSet().range(key, 0, 0);
       /* Integer userId = null;
        for (String u : userSet) {
            userId = Integer.parseInt(u);
        }*/
        // jdk8新特性
        Integer userId = 0;
        try {
            userId = Integer.parseInt(userSet.stream().findFirst().get());
        } catch (Exception e) {
            e.printStackTrace();

        }
        return userId;
    }

    // 处理自动维修工单
    @Override
    public void autoRepairTask(VmStatusContract statusContract) {

        // 根据设备编号查询区域信息
        VmVO vmVO = vmServiceFeignClient.getInnerCode(statusContract.getInnerCode());
        if (ObjectUtil.isEmpty(vmVO)) {
            throw new LogicException("该设备不存在");
        }

        // 获取当天该设备区域下最少工单量的工作人员
        Integer userId = getLeastUser(vmVO.getRegionId(), true);

        // 1.遍历设备配件信息
        for (StatusInfo statusInfo : statusContract.getStatusInfo()) {
            // 2.判断配件状态
            if (statusInfo.isStatus() == false) {
                // 设备故障，创建工单
                TaskViewModel taskViewModel = new TaskViewModel();
                taskViewModel.setCreateType(0);// 自动工单
                taskViewModel.setInnerCode(statusContract.getInnerCode()); // 设备编号
                taskViewModel.setUserId(userId); // 谁干活
                taskViewModel.setAssignorId(0); // 自动工单创建人为0
                taskViewModel.setProductType(VMSystem.TASK_TYPE_REPAIR);
                taskViewModel.setDesc("自动维修工单");
                // 调用创建工单的方法
                create(taskViewModel);
                // 结束循环
                break;
            }

        }
    }


    // 处理自动补货工单
    @Override
    public void autoSupplyTask(SupplyContract supplyContract) {

        // 查询设备
        VmVO vmVO = vmServiceFeignClient.getInnerCode(supplyContract.getInnerCode());
        if (ObjectUtil.isEmpty(vmVO)) {
            throw new LogicException("此设备不存在");
        }

        // 查询当天此设备区域工单量最少的人
        Integer userId = getLeastUser(vmVO.getRegionId(), false);

        // 创建工单对象
        TaskViewModel taskViewModel = new TaskViewModel();
        taskViewModel.setCreateType(0); // 自动工单
        taskViewModel.setInnerCode(supplyContract.getInnerCode()); // 设备编号
        taskViewModel.setUserId(userId); // 谁干活
        taskViewModel.setAssignorId(0); // 自动创建
        taskViewModel.setProductType(VMSystem.TASK_TYPE_SUPPLY); // 补货工单类型
        taskViewModel.setDesc("自动补货工单");

        // 添加补货工单详情列表
        List<TaskDetailsViewModel> details = new ArrayList<>();
        for (SupplyChannel sc : supplyContract.getSupplyData()) {
            // 创建
            TaskDetailsViewModel taskDetailsViewModel = new TaskDetailsViewModel();
            taskDetailsViewModel.setChannelCode(sc.getChannelId()); // 货道号
            taskDetailsViewModel.setExpectCapacity(sc.getCapacity()); // 补几件
            taskDetailsViewModel.setSkuId(sc.getSkuId());// 商品id
            taskDetailsViewModel.setSkuName(sc.getSkuName());// 商品名称
            taskDetailsViewModel.setSkuImage(sc.getSkuImage());// 商品图片
            details.add(taskDetailsViewModel);
        }
        taskViewModel.setDetails(details);


        // 调用创建工单方法
        create(taskViewModel);
    }

    // 统计工单数量
    private Integer taskCount(LocalDateTime start, LocalDateTime end, Boolean isRepair, Integer taskStatus) {
        LambdaQueryWrapper<TaskEntity> qw = new LambdaQueryWrapper<>();
        qw.between(TaskEntity::getCreateTime, start, end);
        // 判断是否为运维工单
        if (isRepair) {
            // 运维工单
            qw.ne(TaskEntity::getProductTypeId, VMSystem.TASK_TYPE_SUPPLY);
        } else {
            // 运营工单
            qw.eq(TaskEntity::getProductTypeId, VMSystem.TASK_TYPE_SUPPLY);
        }
        // 判断是否提供工单状态
        qw.eq(ObjectUtil.isNotEmpty(taskStatus), TaskEntity::getTaskStatus, taskStatus);

        return this.count(qw);
    }

    //根据工单状态，获取用户指定时间段范围的工单数
    private Integer getCountByUserId(Integer userId, Integer taskStatus, LocalDate start, LocalDateTime end) {
        var qw = new LambdaQueryWrapper<TaskEntity>();
        qw
                .ge(TaskEntity::getUpdateTime, start)
                .le(TaskEntity::getUpdateTime, end);
        if (taskStatus != null) {
            qw.eq(TaskEntity::getTaskStatus, taskStatus);
        }
        if (userId != null) {
            qw.eq(TaskEntity::getUserId, userId);
        }
        return this.count(qw);
    }

    /**
     * 按时间和状态进行统计
     *
     * @param start
     * @param taskStatus
     * @return
     */
    private int count(LocalDate start, Integer taskStatus) {
        var qw = new LambdaQueryWrapper<TaskEntity>();
        qw
                .ge(TaskEntity::getUpdateTime, start)
                .lt(TaskEntity::getUpdateTime, start.plusDays(1))
                .eq(TaskEntity::getTaskStatus, taskStatus);
        return this.count(qw);
    }

    /**
     * 生成工单编号
     *
     * @return
     */
    private String generateTaskCode() {
        //      日期字符串
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        把当前序号存入redis中 ，key的值
        String key = "lkd:task:code:" + date;
        Long value = redisTemplate.opsForValue().increment(key);
        if (value == 1) {
//            只在第一次操作redis的时候，设置有效期
            redisTemplate.expire(key, 1, TimeUnit.DAYS);
        }
//        返回的工单号 日期+序号 ，序号如果不够4位，前面补0
        return date + Strings.padStart(value.toString(), 4, '0');
    }

    /**
     * 判断工单基础数据
     **/
    private void verifyTaskMsg(TaskViewModel taskViewModel) {

        if (StringUtils.isEmpty(taskViewModel.getInnerCode())) {
            throw new LogicException("售货机编号不能为空");
        }

        if (ObjectUtils.isEmpty(taskViewModel.getUserId())) {
            throw new LogicException("必须要指定员工");

        }
        if (ObjectUtils.isEmpty(taskViewModel.getProductType())) {
            throw new LogicException("工单类型不能为空");
        }

        if (StringUtils.isEmpty(taskViewModel.getDesc())) {
            throw new LogicException("工单描述不能为空");
        }

        if (VMSystem.TASK_TYPE_SUPPLY == taskViewModel.getProductType()) {
            if (CollectionUtils.isEmpty(taskViewModel.getDetails())) {
                throw new LogicException("补货工单补货详情不能为空");
            }
        }
    }

    /**
     * 创建工单校验
     *
     * @param vmStatus
     * @param productType
     * @throws LogicException
     */
    private void checkCreateTask(Integer vmStatus, int productType) throws LogicException {
        //如果是投放工单，状态为运营
        if (productType == VMSystem.TASK_TYPE_DEPLOY && vmStatus == VMSystem.VM_STATUS_RUNNING) {
            throw new LogicException("该设备已在运营");
        }

        //如果是补货工单，状态不是运营状态
        if (productType == VMSystem.TASK_TYPE_SUPPLY && vmStatus != VMSystem.VM_STATUS_RUNNING) {
            throw new LogicException("该设备不在运营状态");
        }

        //如果是撤机工单，状态不是运营状态
        if (productType == VMSystem.TASK_TYPE_REVOKE && vmStatus != VMSystem.VM_STATUS_RUNNING) {
            throw new LogicException("该设备不在运营状态");
        }
    }

    /**
     * 同一台设备下是否存在未完成的工单
     *
     * @param innerCode
     * @param productionType
     * @return
     */
    private boolean hasTask(String innerCode, int productionType) {
        QueryWrapper<TaskEntity> qw = new QueryWrapper<>();
        qw.lambda()
                .select(TaskEntity::getTaskId)
                .eq(TaskEntity::getInnerCode, innerCode)
                .eq(TaskEntity::getProductTypeId, productionType)
                .le(TaskEntity::getTaskStatus, VMSystem.TASK_STATUS_PROGRESS);
        return this.count(qw) > 0;
    }

    // 更新工单量列表
    private void updateTaskZSet(TaskEntity task, int score) {
        // 1.确定角色code（根据工单类型确认）
        String roleCode = VMSystem.USER_ROLE_REPAIRER_CODE; // 初始值 运维角色
        if (task.getProductTypeId().equals(VMSystem.TASK_TYPE_SUPPLY)) {
            // 运营角色
            roleCode = VMSystem.USER_ROLE_SUPPLY_ODE;
        }
        // 2.获取key
        String key = VMSystem.generateKey(task.getRegionId(), roleCode);
        // 3.zset数量+1 或-1
        redisTemplate.opsForZSet().incrementScore(key, task.getUserId().toString(), score);
    }
}
