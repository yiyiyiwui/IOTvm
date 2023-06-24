package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author LKD
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tb_sku",autoResultMap = true,resultMap = "skuMap")
public class SkuEntity implements Serializable {

    @TableId("sku_id")
    private Long skuId;

    /**
     * 商品名称
     */
    @TableField("sku_name")
    private String skuName;

    /**
     * 商品图片
     */
    @TableField("sku_image")
    private String skuImage;

    /**
     * 基础价格
     */
    @TableField("price")
    private Integer price;

    /**
     * 商品类别Id
     */
    @TableField("class_id")
    private Integer classId;

    /**
     * 是否打折促销
     */
    @TableField("is_discount")
    private Boolean discount;

    /**
     * 净含量
     */
    @TableField("unit")
    private String unit;

    @TableField("brand_Name")
    private String brandName;

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
    private SkuClassEntity skuClass;//商品类别
}
