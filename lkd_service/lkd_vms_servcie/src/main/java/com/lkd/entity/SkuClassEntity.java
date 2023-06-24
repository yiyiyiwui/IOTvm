package com.lkd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

@Data
@TableName(value = "tb_sku_class")
public class SkuClassEntity implements Serializable{

    @TableId(value = "class_id",type = IdType.AUTO)
    private Integer classId;//id
    @TableField(value = "class_name")
    private String className;//类别名称
    @TableField(value = "parent_id")
    private Integer parentId;//上级id

}
