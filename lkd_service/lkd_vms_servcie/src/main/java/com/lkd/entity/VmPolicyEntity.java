package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 机器和策略关联表
 * </p>
 *
 * @author LKD
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("tb_vm_policy")
public class VmPolicyEntity implements Serializable {


      /**
     * id
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Long id;

      /**
     * 售货机id
     */
      @TableField("vm_id")
    private Long vmId;

      /**
     * 售货机编号
     */
      @TableField("inner_code")
    private String innerCode;

      /**
     * 策略id
     */
      @TableField("policy_id")
    private Integer policyId;

      /**
     * 策略名称
     */
      @TableField("policy_name")
    private String policyName;

      /**
     * 折扣
     */
      @TableField("discount")
    private Integer discount;

      @TableField(value = "create_time", fill = FieldFill.INSERT)
      private LocalDateTime createTime;

      @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
      private LocalDateTime updateTime;


}
