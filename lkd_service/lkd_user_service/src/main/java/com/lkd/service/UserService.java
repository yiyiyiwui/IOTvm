package com.lkd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkd.entity.UserEntity;
import com.lkd.http.controller.vo.*;

import java.io.IOException;
import java.util.List;

public interface UserService extends IService<UserEntity> {
    /**
     * 获取所有运营人员数量
     */
    Integer getOperatorCount();

    /**
     * 获取所有维修员数量
     * @return
     */
    Integer getRepairerCount();

    /**
     * 分页查询
     * @param pageIndex
     * @param pageSize
     * @param userName
     * @return
     */
    Pager<UserEntity> findPage(long pageIndex, long pageSize, String userName,Integer roleId,Boolean isRepair);

    /**
     * 获取某区域下所有运营人员
     * @param regionId
     * @return
     */
    List<UserVO> getOperatorList(Long regionId);

    /**
     * 获取某区域下所有运维人员
     * @param regionId
     * @return
     */
    List<UserVO> getRepairerList(Long regionId);

    /**
     * 获取某区域下维修员/运营员总数
     * @param isRepair
     * @return
     */
    Integer getCountByRegion(Long regionId,Boolean isRepair);

    /**
     * 登录
     * @param req
     * @return
     */
    LoginResp login(LoginReq req) throws IOException;




    /**
     * 查询工作量列表
     * @param pageIndex
     * @param pageSize
     * @param userName
     * @param roleId
     * @param isRepair
     * @return
     */
    Pager<UserWorkVO> searchUserWork(Long pageIndex, Long pageSize, String userName, Integer roleId, Boolean isRepair  );

    // 发送短信验证码
    void sendSms(String mobile);
}
