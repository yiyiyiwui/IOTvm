package com.lkd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lkd.dao.ChannelDao;
import com.lkd.http.controller.vo.ChannelConfigReq;
import com.lkd.entity.ChannelEntity;
import com.lkd.entity.SkuEntity;
import com.lkd.exception.LogicException;
import com.lkd.http.controller.vo.ChannelVO;
import com.lkd.http.controller.vo.SkuVO;
import com.lkd.service.ChannelService;
import com.lkd.service.SkuService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelServiceImpl extends ServiceImpl<ChannelDao, ChannelEntity> implements ChannelService {

    @Autowired
    private SkuService skuService;

    // 根据售货机编号查询货道列表
    @Override
    public List<ChannelEntity> channelList(String innerCode) {
        // 0.参数校验
        if (StrUtil.isEmpty(innerCode)) {
            throw new LogicException("非法参数");
        }
        // 1.查询该售货机下所有的货道
        LambdaQueryWrapper<ChannelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChannelEntity::getInnerCode, innerCode);
        List<ChannelEntity> channelEntityList = this.list(queryWrapper);
        // 2.遍历货道，查询每一个货道关联的商品
        for (ChannelEntity channelEntity : channelEntityList) {
            // 查询商品
            SkuEntity skuEntity = skuService.getById(channelEntity.getSkuId());
            channelEntity.setSku(skuEntity);
        }

        // 3.返回货道列表
        return channelEntityList;
    }

    // 货道设置
    @Override
    public Boolean channelConfig(ChannelConfigReq channelConfigReq) {
        // 1.遍历货道列表
        for (ChannelEntity channelEntity : channelConfigReq.getChannelList()) {
            // 查询货道  innerCode + channelCode
            LambdaQueryWrapper<ChannelEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(ChannelEntity::getInnerCode, channelConfigReq.getInnerCode())
                    .eq(ChannelEntity::getChannelCode, channelEntity.getChannelCode());
            ChannelEntity oldChannel = this.getOne(queryWrapper);

            // 查询商品
            if (channelEntity.getSkuId() == 0) { // 没商品，删除旧的
                oldChannel.setSkuId(0l);
                oldChannel.setPrice(0);
            } else {  // 有商品，新的覆盖旧的
                SkuEntity skuEntity = skuService.getById(channelEntity.getSkuId());
                // 将新商品设置到货道中
                oldChannel.setSkuId(skuEntity.getSkuId());
                oldChannel.setPrice(skuEntity.getPrice());
            }

            // 更新数据库
            this.updateById(oldChannel);
        }
        return true;
    }

    // 售货机商品是否还有库存
    @Override
    public ChannelVO hasCapacity(String innerCode, Long skuId) {
        // 1.先查询货道
        List<ChannelEntity> channelList = this.lambdaQuery()
                .eq(ChannelEntity::getInnerCode, innerCode)
                .eq(ChannelEntity::getSkuId, skuId)
                .gt(ChannelEntity::getCurrentCapacity, 0)
                .list();
        // 2.取出其中一个
        if (CollUtil.isEmpty(channelList)) {
            return null;
        }
        ChannelEntity channel = channelList.stream().findFirst().get();
        // 3.转换vo
        ChannelVO channelVO = BeanUtil.copyProperties(channel, ChannelVO.class);
        // 3-1 查询商品
        SkuEntity sku = skuService.getById(channel.getSkuId());
        // 3-2封装skuVO
        SkuVO skuVO = BeanUtil.copyProperties(sku, SkuVO.class);
        skuVO.setImage(sku.getSkuImage()); // 图片
        skuVO.setRealPrice(sku.getPrice()); // 真实售价
        skuVO.setCapacity(channel.getCurrentCapacity()); // 商品余量
        // 3-3设置到channelVo
        channelVO.setSkuVO(skuVO);
        // 4.返回
        return channelVO;
    }
}
