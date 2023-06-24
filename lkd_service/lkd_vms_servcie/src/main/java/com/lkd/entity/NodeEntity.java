package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 点位表
 * </p>
 *
 * @author LKD
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tb_node")
public class NodeEntity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 点位名称
     */
    @TableField("name")
    private String name;

    /**
     * 点位详细地址
     */
    @TableField("addr")
    private String addr;

    /**
     * 所在区域Id
     */
    @TableField("area_code")
    private String areaCode;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private Integer createUserId;

    /**
     * 区域id
     */
    @TableField("region_id")
    private Long regionId;

    /**
     * 商圈id
     */
    @TableField("business_id")
    private Integer businessId;

    /**
     * 点位主Id
     */
    @TableField("owner_id")
    private Integer ownerId;

    /**
     * 点位主名称
     */
    @TableField("owner_name")
    private String ownerName;

    /**
     * 更改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 管理其他表获取数据，返回给前端
    @TableField(exist = false)
    private long vmCount;
    @TableField(exist = false)
    private RegionEntity region;
    @TableField(exist = false)
    private BusinessTypeEntity businessType;
}
