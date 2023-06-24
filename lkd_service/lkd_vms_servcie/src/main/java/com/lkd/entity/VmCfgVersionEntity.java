package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 售货机配置版本
 * </p>
 *
 * @author LKD
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("tb_vm_cfg_version")
public class VmCfgVersionEntity implements Serializable {


      /**
     * id
     */
        @TableId(value = "version_id", type = IdType.AUTO)
      private Long versionId;

      /**
     * 售货机Id
     */
      @TableField("vm_id")
    private Long vmId;

      /**
     * 售货机编号
     */
      @TableField("inner_code")
    private String innerCode;

      /**
     * 货道配置版本
     */
      @TableField("channel_cfg_version")
    private Long channelCfgVersion;

      /**
     * 基础配置版本
     */
      @TableField("basecfg_version")
    private Long basecfgVersion;

      /**
     * 商品配置版本
     */
      @TableField("sku_cfg_version")
    private Long skuCfgVersion;

      /**
     * 价格配置版本
     */
      @TableField("price_cfg_version")
    private Long priceCfgVersion;

      /**
     * 补货版本
     */
      @TableField("supply_version")
    private Long supplyVersion;


}
