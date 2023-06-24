package com.lkd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkd.entity.SkuEntity;
import com.lkd.http.controller.vo.Pager;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author LKD
 */
public interface SkuService extends IService<SkuEntity> {

    // 商品搜索
    Pager<SkuEntity> search(Integer pageIndex, Integer pageSize, String skuName, Long classId);
}
