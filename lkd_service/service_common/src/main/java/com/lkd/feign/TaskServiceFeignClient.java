package com.lkd.feign;

import com.lkd.http.controller.vo.UserWorkVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "task-service")
public interface TaskServiceFeignClient {

    @GetMapping("/task/userWork")
    UserWorkVO getUserWork(@RequestParam Integer userId, @RequestParam String start, @RequestParam String end);

}
