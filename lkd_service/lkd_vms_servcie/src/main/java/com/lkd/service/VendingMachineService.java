package com.lkd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkd.contract.SupplyContract;
import com.lkd.contract.TaskCompleteContract;
import com.lkd.contract.VendoutContract;
import com.lkd.contract.VendoutResultContract;
import com.lkd.http.controller.vo.CreateVMReq;
import com.lkd.entity.VendingMachineEntity;
import com.lkd.http.controller.vo.Pager;
import com.lkd.http.controller.vo.SkuVO;
import com.lkd.http.controller.vo.VmVO;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author LKD
 */
public interface VendingMachineService extends IService<VendingMachineEntity> {

    // 分页查询
    Pager<VendingMachineEntity> search(Integer pageIndex, Integer pageSize, Integer status, String innerCode);

    // 新增售货机
    Boolean addVM(VendingMachineEntity vendingMachineEntity);

    // 根据设备编号，查询VmVo
    VmVO getInnerCode(String innerCode);

    // 根据工单类型，修改设备状态
    Boolean updateVMStatus(TaskCompleteContract taskCompleteContract);

    // 补货
    Boolean supply(SupplyContract supplyContract);


    // 自动补货任务
    void autoSupplyTask();

    // 自动补货任务 分片版本
    void autoSupplyTask(int total, int index);

    // 获取售货机商品列表
    List<SkuVO> getSkuListByInnerCode(String innerCode);

    // 处理出货逻辑
    Boolean vendout(VendoutContract vendoutContract);


    // 售货机微服务处理出货结果
    Boolean vendoutResult(VendoutResultContract vendoutContract);
}
