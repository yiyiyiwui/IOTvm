package com.lkd.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lkd.dao.NodeDao;
import com.lkd.entity.BusinessTypeEntity;
import com.lkd.entity.NodeEntity;
import com.lkd.entity.RegionEntity;
import com.lkd.entity.VendingMachineEntity;
import com.lkd.exception.LogicException;
import com.lkd.http.controller.vo.Pager;
import com.lkd.service.BusinessTypeService;
import com.lkd.service.NodeService;
import com.lkd.service.RegionService;
import com.lkd.service.VendingMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NodeServiceImpl extends ServiceImpl<NodeDao, NodeEntity> implements NodeService {

    // 售货机
    @Autowired
    private VendingMachineService vendingMachineService;

    // 区域
    @Autowired
    private RegionService regionService;

    // 商圈
    @Autowired
    private BusinessTypeService businessTypeService;

    // 分页查询
    @Override
    public Pager<NodeEntity> search(Integer pageIndex, Integer pageSize, String name, Long regionId) {
        // 1.点位 分页+条件查询
        Page<NodeEntity> page = new Page<>(pageIndex, pageSize);
        LambdaQueryWrapper<NodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        // name条件
        queryWrapper.like(StrUtil.isNotEmpty(name), NodeEntity::getName, name);
        // regionId条件
        queryWrapper.eq(ObjectUtil.isNotEmpty(regionId), NodeEntity::getRegionId, regionId);
        // 查询
        this.page(page, queryWrapper);
        //  2.遍历点位列表
        for (NodeEntity nodeEntity : page.getRecords()) {
            // 查询售货机数量
            LambdaQueryWrapper<VendingMachineEntity> vQuery = new LambdaQueryWrapper<>();
            vQuery.eq(VendingMachineEntity::getNodeId, nodeEntity.getId());
            int vCount = vendingMachineService.count(vQuery);
            nodeEntity.setVmCount(vCount);
            // 查询区域信息
            LambdaQueryWrapper<RegionEntity> rQuery = new LambdaQueryWrapper<>();
            rQuery.eq(RegionEntity::getId, nodeEntity.getRegionId());
            RegionEntity regionEntity = regionService.getOne(rQuery);
            nodeEntity.setRegion(regionEntity);
            // 查询商圈信息
            BusinessTypeEntity businessTypeEntity = businessTypeService.getById(nodeEntity.getBusinessId());
            nodeEntity.setBusinessType(businessTypeEntity);
        }
        // 3.返回分页对象
        return Pager.build(page);
    }

    // 点位详情
    @Override
    public List<VendingMachineEntity> nodeDetail(Long id) {
        // 0.参数校验
        if (ObjectUtil.isEmpty(id)) {
            throw new LogicException("参数非法");
        }
        // 1.构建条件
        LambdaQueryWrapper<VendingMachineEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VendingMachineEntity::getNodeId, id);
        // 2.执行查询并返回
        return vendingMachineService.list(queryWrapper);
    }

    // 新增点位
    @Override
    public Boolean addNode(NodeEntity nodeEntity) {
        // 1.参数校验
        // 2.业务校验
        // 2-1 商圈是否存在
        BusinessTypeEntity businessTypeEntity = businessTypeService.getById(nodeEntity.getBusinessId());
        if (ObjectUtil.isEmpty(businessTypeEntity)) {
            throw new LogicException("商圈信息不存在");
        }
        // 2-2 区域是否存在
        RegionEntity regionEntity = regionService.getById(nodeEntity.getRegionId());
        if (ObjectUtil.isEmpty(regionEntity)) {
            throw new LogicException("区域信息不存在");
        }
        // 2-3 节点名是否重复
        LambdaQueryWrapper<NodeEntity> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery.eq(NodeEntity::getName, nodeEntity.getName());
        NodeEntity oldNodeEntity = this.getOne(nodeQuery);
        if (ObjectUtil.isNotEmpty(oldNodeEntity)) {
            throw new LogicException("节点名已被使用");
        }

        // 3.保存数据库
        boolean isSuccess = this.save(nodeEntity);
        return isSuccess;
    }

    // 修改点位
    @Override
    public Boolean updateNode(Long id, NodeEntity nodeEntity) {
        // 1.参数校验
        // 2.业务校验
        // 2-1 商圈是否存在
        BusinessTypeEntity businessTypeEntity = businessTypeService.getById(nodeEntity.getBusinessId());
        if (ObjectUtil.isEmpty(businessTypeEntity)) {
            throw new LogicException("商圈信息不存在");
        }
        // 2-2 区域是否存在
        RegionEntity regionEntity = regionService.getById(nodeEntity.getRegionId());
        if (ObjectUtil.isEmpty(regionEntity)) {
            throw new LogicException("区域信息不存在");
        }
        // 2-3 节点名是否重名
        // where name = '顺义黑马' and id != 自己
        LambdaQueryWrapper<NodeEntity> nodeQuery = new LambdaQueryWrapper<>();
        nodeQuery
                .eq(NodeEntity::getName, nodeEntity.getName())
                .ne(NodeEntity::getId, id);
        NodeEntity oldNodeEntity = this.getOne(nodeQuery);
        if (ObjectUtil.isNotEmpty(oldNodeEntity)) {
            throw new LogicException("节点名已被使用");
        }
        //  3.修改数据库
        nodeEntity.setId(id);
        return this.updateById(nodeEntity);
    }

    // 删除点位
    @Override
    public Boolean deleteNode(Long id) {
        // 1.参数校验
        // 2.业务校验，点位下有售货机，不能删除
        LambdaQueryWrapper<VendingMachineEntity> vQuery = new LambdaQueryWrapper<>();
        vQuery.eq(VendingMachineEntity::getNodeId, id);
        int vCount = vendingMachineService.count(vQuery);
        if (vCount>0) {
            throw new LogicException("该点位下有售货机，无法删除");
        }
        // 3.删除并返回
        return this.removeById(id);
    }
}
