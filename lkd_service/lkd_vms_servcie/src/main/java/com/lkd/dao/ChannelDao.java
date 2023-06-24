package com.lkd.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lkd.entity.ChannelEntity;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 售货机货道表 Mapper 接口
 * </p>
 *
 * @author LKD
 */
public interface ChannelDao extends BaseMapper<ChannelEntity> {

    @Select("select * from tb_channel where inner_code=#{innerCode}")
    @Results(id="channelMap",value = {
            @Result(property = "channelId",column = "channel_id"),
            @Result(property = "skuId",column = "sku_id"),
            @Result(property = "sku",column = "sku_id",one = @One(select = "com.lkd.dao.SkuDao.getById"))
    })
    List<ChannelEntity> getChannelsByInnerCode(String innerCode);
}
