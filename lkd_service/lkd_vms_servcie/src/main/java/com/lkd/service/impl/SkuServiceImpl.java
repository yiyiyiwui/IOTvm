package com.lkd.service.impl;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.lkd.dao.SkuDao;
import com.lkd.entity.SkuClassEntity;
import com.lkd.entity.SkuEntity;
import com.lkd.service.SkuClassService;
import com.lkd.service.SkuService;
import com.lkd.http.controller.vo.Pager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuDao, SkuEntity> implements SkuService {


    @Autowired
    private SkuClassService skuClassService;

    // 商品搜索
    @Override
    public Pager<SkuEntity> search(Integer pageIndex, Integer pageSize, String skuName, Long classId) {
        // 1.查询商品信息
        // 1-1 分页
        Page<SkuEntity> page = new Page<>(pageIndex, pageSize);
        // 1-2 条件
        LambdaQueryWrapper<SkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StrUtil.isNotEmpty(skuName),SkuEntity::getSkuName,skuName)
                .eq(ObjectUtil.isNotEmpty(classId),SkuEntity::getClassId,classId);
        // 1-3 查询
        this.page(page,queryWrapper);
        // 2.遍历商品列表
       /* for (SkuEntity sku : page.getRecords()) {
            // 查询商品类型对象
            SkuClassEntity skuClass = skuClassService.getById(sku.getClassId());
            sku.setSkuClass(skuClass);
        }*/
        // 3.返回分页对象
        return Pager.build(page);
    }
}
