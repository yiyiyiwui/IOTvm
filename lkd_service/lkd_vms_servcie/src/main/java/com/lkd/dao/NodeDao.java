package com.lkd.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lkd.entity.NodeEntity;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 点位表 Mapper 接口
 * </p>
 *
 * @author LKD
 */
public interface NodeDao extends BaseMapper<NodeEntity> {
    
}
