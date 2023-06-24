package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 策略表
 * </p>
 *
 * @author LKD
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("tb_policy")
public class PolicyEntity implements Serializable {


      /**
     * 策略id
     */
        @TableId(value = "policy_id", type = IdType.AUTO)
      private Integer policyId;

      /**
     * 策略名称
     */
      @TableField("policy_name")
    private String policyName;

      /**
     * 折扣，如：80代表8折
     */
      @TableField("discount")
    private Integer discount;

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
