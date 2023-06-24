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
 * 出货流水
 * </p>
 *
 * @author LKD
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("tb_vendout_running")
public class VendoutRunningEntity implements Serializable {


      /**
     * id
     */
        @TableId("id")
      private Long id;

      /**
     * 订单编号
     */
      @TableField("order_no")
    private String orderNo;

      /**
     * 售货机编号
     */
      @TableField("inner_code")
    private String innerCode;

      /**
     * 状态
     */
      @TableField("status")
    private Boolean status;

      /**
     * 商品编号
     */
      @TableField("sku_id")
    private Long skuId;

      /**
     * 商品名称
     */
      @TableField("sku_name")
    private String skuName;

      /**
     * 价格
     */
      @TableField("price")
    private Integer price;

      /**
     * 创建时间
     */
        @TableField(value = "create_time", fill = FieldFill.INSERT)
      private LocalDateTime createTime;

      @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
      private LocalDateTime updateTime;


}
