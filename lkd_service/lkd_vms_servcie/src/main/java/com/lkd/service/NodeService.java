package com.lkd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkd.http.controller.vo.NodeReq;
import com.lkd.entity.NodeEntity;
import com.lkd.entity.VendingMachineEntity;
import com.lkd.http.controller.vo.Pager;

import java.util.List;

/**
 * <p>
 * 点位表 服务类
 * </p>
 *
 * @author LKD
 */
public interface NodeService extends IService<NodeEntity> {

    // 分页查询
    Pager<NodeEntity> search(Integer pageIndex, Integer pageSize, String name, Long regionId);

    // 点位详情
    List<VendingMachineEntity> nodeDetail(Long id);

    // 新增点位
    Boolean addNode(NodeEntity nodeEntity);

    // 修改点位
    Boolean updateNode(Long id, NodeEntity nodeEntity);

    // 删除点位
    Boolean deleteNode(Long id);
}
