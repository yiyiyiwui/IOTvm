package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "tb_order")
public class OrderEntity  implements Serializable{
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;//id
    @TableField(value = "order_no")
    private String orderNo;//订单编号
    @TableField(value = "third_no")
    private String thirdNo;//支付流水号
    @TableField(value = "inner_code")
    private String innerCode;//机器编号
    /**
     * 点位地址
     */
    private String addr;
    private Long skuId;//skuId
    @TableField(value = "sku_name")
    private String skuName;//商品名称
    /**
     * 商品类别Id
     */
    private Integer classId;
    @TableField(value = "status")
    private Integer status;//订单状态:0-创建;1-支付完成;2-出货成功;3-出货失败;
    @TableField(value = "amount")
    private Integer amount;//支付金额
    @TableField(value = "bill")
    private Integer bill;//分账金额
    @TableField(value = "price")
    private Integer price;//商品金额
    @TableField(value = "pay_type")
    private String payType;//支付类型，1支付宝 2微信
    @TableField(value = "pay_status")
    private Integer payStatus;//支付状态，0-未支付;1-支付完成;2-退款中;3-退款完成
    /**
     * 点位Id
     */
    private Long nodeId;
    /**
     * 点位名称
     */
    private String nodeName;
    /**
     * 所属区域Id
     */
    private Long regionId;
    /**
     * 所属区域名称
     */
    private String regionName;
    /**
     * 所属商圈Id
     */
    private Integer businessId;
    /**
     * 所属商圈名称
     */
    private String businessName;
    /**
     * 合作商Id
     */
    private Integer ownerId;
    /**
     * 微信用户openId
     */
    private String openId;
    /**
     * 取消原因
     */
    private String cancelDesc;

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
}
