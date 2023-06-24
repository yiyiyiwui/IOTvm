package com.lkd.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lkd.entity.VendingMachineEntity;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author LKD
 */
public interface VendingMachineDao extends BaseMapper<VendingMachineEntity> {

    // 手动映射
    @Results(id = "vmResult", value = {
            @Result(column = "vm_type", property = "vmType"),
            @Result(column = "node_id", property = "nodeId"),
            @Result(column = "region_id", property = "regionId"),
            @Result(column = "vm_type", property = "type", one = @One(select = "com.lkd.dao.VmTypeDao.selectById")),
            @Result(column = "node_id", property = "node", one = @One(select = "com.lkd.dao.NodeDao.selectById")),
            @Result(column = "region_id", property = "region", one = @One(select = "com.lkd.dao.RegionDao.selectById"))
    })
    @Select("select * from tb_vending_machine where id=#{id}")
    public VendingMachineEntity findById(Long id);
}
