package com.lkd.http.controller;

import com.lkd.entity.VendingMachineEntity;
import com.lkd.http.controller.vo.Pager;
import com.lkd.http.controller.vo.SkuVO;
import com.lkd.http.controller.vo.VmVO;
import com.lkd.service.VendingMachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/vm")
public class VendingMachineController {

    @Autowired
    private VendingMachineService vendingMachineService;

    // 分页查询
    @GetMapping("/search")
    public Pager<VendingMachineEntity> search(
            @RequestParam(defaultValue = "1") Integer pageIndex,
            @RequestParam(defaultValue = "10") Integer pageSize,
            Integer status,
            String innerCode) {
        return vendingMachineService.search(pageIndex, pageSize, status, innerCode);
    }


    // 新增售货机
    @PostMapping
    public Boolean addVM(@RequestBody VendingMachineEntity vendingMachineEntity){
        return vendingMachineService.addVM(vendingMachineEntity);
    }


    // 根据设备编号，查询VmVo
    @GetMapping("/getInnerCode/{innerCode}")
    public VmVO getInnerCode(@PathVariable("innerCode") String innerCode){
        return vendingMachineService.getInnerCode(innerCode);
    }

    // 获取售货机商品列表
    @GetMapping("/skuList/{innerCode}")
    public List<SkuVO> getSkuListByInnerCode(@PathVariable("innerCode") String innerCode){
        return vendingMachineService.getSkuListByInnerCode(innerCode);
    }

}