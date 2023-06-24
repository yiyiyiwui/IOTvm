package com.lkd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkd.http.controller.vo.ChannelConfigReq;
import com.lkd.entity.ChannelEntity;
import com.lkd.http.controller.vo.ChannelVO;

import java.util.List;

/**
 * <p>
 * 售货机货道表 服务类
 * </p>
 *
 * @author LKD
 */
public interface ChannelService extends IService<ChannelEntity> {

    // 根据售货机编号查询货道列表
    List<ChannelEntity> channelList(String innerCode);

    // 货道设置
    Boolean channelConfig(ChannelConfigReq channelConfigReq);


    // 售货机商品是否还有库存
    ChannelVO hasCapacity(String innerCode, Long skuId);
}
