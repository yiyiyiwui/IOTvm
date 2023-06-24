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
 * 区域表
 * </p>
 *
 * @author LKD
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("tb_area")
public class AreaEntity implements Serializable {


      @TableId(value = "id", type = IdType.AUTO)
      private Integer id;

      /**
     * 父Id
     */
      @TableField("parent_id")
    private Integer parentId;

      /**
     * 区域名称
     */
      @TableField("area_name")
    private String areaName;

      /**
     * 地区编码
     */
      @TableField("ad_code")
    private String adCode;

      /**
     * 城市区号
     */
      @TableField("city_code")
    private String cityCode;

      /**
     * 地区级别
     */
      @TableField("area_level")
    private String areaLevel;


}
