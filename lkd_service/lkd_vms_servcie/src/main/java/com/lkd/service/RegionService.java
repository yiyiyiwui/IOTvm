package com.lkd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkd.http.controller.vo.RegionReq;
import com.lkd.entity.RegionEntity;
import com.lkd.http.controller.vo.Pager;

/**
 * <p>
 * 运营区域 服务类
 * </p>
 *
 * @author LKD
 */
public interface RegionService extends IService<RegionEntity> {

    Pager<RegionEntity> search(Long pageIndex, Long pageSize, String name);



    /**
     * 添加区域信息
     * @param regionEntity RegionEntity 区域实体类
     * @return boolean 操作是否成功
     */
    boolean createRegion(RegionEntity regionEntity);



    /**
     * 修改区域
     * @param regionId
     * @param regionReq
     * @return
     */
    boolean updateRegion(Long regionId, RegionReq regionReq);


    /**
     * 根据id删除区域信息
     * @param regionId Long 区域id值
     * @return boolean 操作是否成功
     */
    boolean delRegion(Long regionId);
}
