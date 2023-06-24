package com.lkd.feign;

import com.lkd.http.controller.vo.PartnerVO;
import com.lkd.http.controller.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "user-service")
public interface UserServiceFeignClient {

    /**
     * 获取运营员数量
     *
     * @return
     */
    @GetMapping("/user/operaterCount")
    Integer getOperatorCount();

    /**
     * 获取维修员数量
     *
     * @return
     */
    @GetMapping("/user/repairerCount")
    Integer getRepairerCount();

    // 根据id查询员工
    @GetMapping("/user/{id}")
    public UserVO findById(@PathVariable Integer id);

    // 获取合作商名称
    @GetMapping("/partner/{id}")
    public PartnerVO getPartner(@PathVariable Integer id);
}