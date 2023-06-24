package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 售货机货道表
 * </p>
 *
 * @author LKD
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tb_channel")
public class ChannelEntity implements Serializable {

    /**
     * 表Id
     */
    @TableId(value = "channel_id", type = IdType.AUTO)
    private Long channelId;

    /**
     * 货道编号
     */
    @TableField("channel_code")
    private String channelCode;

    /**
     * 商品Id
     */
    @TableField("sku_id")
    private Long skuId;

    /**
     * 售货机Id
     */
    @TableField("vm_id")
    private Long vmId;

    /**
     * 售货机软编号
     */
    @TableField("inner_code")
    private String innerCode;

    /**
     * 货道最大容量
     */
    @TableField("max_capacity")
    private Integer maxCapacity;

    /**
     * 货道商品真实售价
     */
    @TableField("price")
    private Integer price;

    /**
     * 货道当前容量
     */
    @TableField("current_capacity")
    private Integer currentCapacity;

    /**
     * 上次补货时间
     */
    @TableField("last_supply_time")
    private LocalDateTime lastSupplyTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private SkuEntity sku;
}
