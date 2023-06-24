package com.lkd.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.lkd.dao.RegionDao;
import com.lkd.http.controller.vo.RegionReq;
import com.lkd.entity.NodeEntity;
import com.lkd.entity.RegionEntity;
import com.lkd.exception.LogicException;
import com.lkd.service.NodeService;
import com.lkd.service.RegionService;
import com.lkd.http.controller.vo.Pager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
public class RegionServiceImpl extends ServiceImpl<RegionDao, RegionEntity> implements RegionService {


    @Autowired
    private NodeService nodeService;

    @Override
    public Pager<RegionEntity> search(Long pageIndex, Long pageSize, String name) {

        // 1.判断分页数据数据
        if (ObjectUtils.isEmpty(pageIndex) || pageIndex < 1) {
            pageIndex = Pager.DEFAULT_PAGE_INDEX;
        }

        if (ObjectUtils.isEmpty(pageSize) || pageSize < 1) {
            pageSize = Pager.DEFAULT_PAGE_SIZE;
        }

        // 2.创建分页对象（MP）
        Page<RegionEntity> page = new Page<>(pageIndex,pageSize);

        // 3.构建查询对象
        LambdaQueryWrapper<RegionEntity> wrapper = new LambdaQueryWrapper<>();
        if(!Strings.isNullOrEmpty(name)){
            wrapper.like(RegionEntity::getName,name);
        }


        // 4.查询数据并返回
        this.page(page,wrapper);


        // 封装点位数量
        for (RegionEntity regionEntity : page.getRecords()) {
            // 统计数量
            LambdaQueryWrapper<NodeEntity> nodeWrapper = new LambdaQueryWrapper<>();
            nodeWrapper.eq(NodeEntity::getRegionId,regionEntity.getId());
            int count = nodeService.count(nodeWrapper);
            regionEntity.setNodeCount(count);
        }


        // 5.转换数据并返回 Pager对象
        return Pager.build(page);

    }


    /*
     * 业务分析：
     *  1.判断关键数据据
     *    区域名称和区域备注
     *  2.判断业务数据
     *    本次操作没有关联后端（数据库）其他数据，无需操作
     *  3.保持区域数据并返回结果
     * */
    @Override
    public boolean createRegion(RegionEntity regionEntity) {
        //1.判断关键数据据
        //   区域名称和区域备注
        if (Strings.isNullOrEmpty(regionEntity.getName())
                || Strings.isNullOrEmpty(regionEntity.getRemark())
        ) {
            throw new LogicException("关键参数与接口不匹配");
        }
        // 2.判断业务数据
        //   本次操作没有关联后端（数据库）其他数据，无需操作
        // 3.返回结果
        return this.save(regionEntity);
    }


    /**
     * 修改区域
     * @param regionId
     * @param regionReq
     * @return
     */
    @Override
    public boolean updateRegion(Long regionId, RegionReq regionReq) {

//        查询name和remark
        LambdaQueryWrapper<RegionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RegionEntity::getName,regionReq.getRegionName()).
                or().
                eq(RegionEntity::getRemark,regionReq.getRemark()).
                ne(RegionEntity::getId,regionId);
        int count = count(queryWrapper);

        if(count>0){
//            存在相同的数据
            log.error("存在相同的数据不能修改！name={}",regionReq.getRegionName());
            throw new LogicException("区域信息已经存在！");
        }

        RegionEntity regionEntity = new RegionEntity();
        regionEntity.setId(regionId);
        regionEntity.setName(regionReq.getRegionName());
        regionEntity.setRemark(regionReq.getRemark());

        return updateById(regionEntity);

    }

    /*
     * 业务分析：
     *  1.判断关键数据据
     *     区域id值
     *  2.判断业务数据
     *    区域信息--判断是否存在
     *  3.删除区域数据并返回结果
     * */
    @Override
    public boolean delRegion(Long regionId) {
        // 1.判断关键数据
        if (ObjectUtils.isEmpty(regionId)) {
            throw new LogicException("关键参数与接口不匹配");
        }

        //        查区域下是否有点位，如果有不能删除
        LambdaQueryWrapper<NodeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NodeEntity::getRegionId,regionId);
        int count = nodeService.count(queryWrapper);
        if(count>0){
            throw new LogicException("区域下包含"+count+"个点位，不能删除");
        }

        // 3.修改区域数据并返回结果
        boolean result = this.removeById(regionId);
        return result;

    }
}
