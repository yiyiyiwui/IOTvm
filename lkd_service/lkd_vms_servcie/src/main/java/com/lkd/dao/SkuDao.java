package com.lkd.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lkd.entity.SkuEntity;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author LKD
 */
public interface SkuDao extends BaseMapper<SkuEntity> {


    // 手动映射
    @Results(id="skuMap",value = {
        @Result(column = "sku_id",property = "skuId",id = true),
        @Result(column = "class_id",property = "skuClass",one = @One(select = "com.lkd.dao.SkuClassDao.selectById"))
    })
    @Select("select * from tb_sku where sku_id = #{id}")
    public SkuEntity findById(Long id);

}
