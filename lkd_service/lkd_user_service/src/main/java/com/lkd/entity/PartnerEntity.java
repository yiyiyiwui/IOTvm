package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "tb_partner")
public class PartnerEntity implements Serializable {
    private static final long serialVersionUID = -2609242735884103559L;
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 合作商名称
     */
    private String name;
    /**
     * 账号
     */
    private String account;
    /**
     * 密码
     */
    private String password;
    /**
     * 联系人
     */
    private String contact;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 联系电话
     */
    private String phone;
    private String email;
    /**
     * 城市
     */
    private String city;
    /**
     * 区县
     */
    private String county;
    /**
     * 所在省
     */
    private String province;
    /**
     * 详细地址
     */
    private String addr;
    /**
     * 分成比例
     */
    private Integer ratio;
    /**
     * 状态
     */
    private Boolean status;
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
    /**
     * 设备数量
     */
    @TableField(exist = false)
    private Integer vmCount;
}
