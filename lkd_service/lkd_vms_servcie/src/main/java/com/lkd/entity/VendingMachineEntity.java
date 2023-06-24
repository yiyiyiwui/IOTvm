package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author LKD
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tb_vending_machine",autoResultMap = true,resultMap = "vmResult")
public class VendingMachineEntity implements Serializable {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 售货机类型
     */
    @TableField("vm_type")
    private Integer vmType;

    /**
     * 售货机软编号
     */
    @TableField("inner_code")
    private String innerCode;

    /**
     * 点位Id
     */
    @TableField("node_id")
    private Long nodeId;

    /**
     * 售货机状态，0:未投放;1-运营;3-撤机
     */
    @TableField("vm_status")
    private Integer vmStatus;

    /**
     * 上次补货时间
     */
    @TableField("last_supply_time")
    private LocalDateTime lastSupplyTime;

    /**
     * 所在城市ID
     */
    @TableField("city_code")
    private String cityCode;

    /**
     * 地区
     */
    @TableField("area_id")
    private Integer areaId;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 创建人姓名
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 商圈Id
     */
    @TableField("business_id")
    private Integer businessId;

    /**
     * 区域Id
     */
    @TableField("region_id")
    private Long regionId;

    /**
     * 点位主Id
     */
    @TableField("owner_id")
    private Integer ownerId;

    /**
     * 所属合作商名称
     */
    @TableField("owner_name")
    private String ownerName;

    /**
     * 客户端连接Id,做emq认证用
     */
    @TableField("client_id")
    private String clientId;

    /**
     * 经度
     */
    @TableField("longitudes")
    private Double longitudes;

    /**
     * 维度
     */
    @TableField("latitude")
    private Double latitude;

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
    private VmTypeEntity type;//售货机类型
    @TableField(exist = false)
    private NodeEntity node;//点位
    @TableField(exist = false)
    private RegionEntity region;//区域
    @TableField(exist = false)
    private String status ="10001"; // 设备运营状态（）

}
