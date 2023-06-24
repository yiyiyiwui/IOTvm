package com.lkd.http.controller;
import com.lkd.http.controller.vo.RegionReq;
import com.lkd.entity.RegionEntity;

import com.lkd.service.RegionService;
import com.lkd.http.controller.vo.Pager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/region")
public class RegionController {
    
    @Autowired
    private RegionService regionService;

    /**
     * 分页搜索
     * @param pageIndex
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/search")
    Pager<RegionEntity> search(
            @RequestParam(value = "pageIndex",required = false,defaultValue = "1") Long pageIndex,
            @RequestParam(value = "pageSize",required = false,defaultValue = "10") Long pageSize,
            @RequestParam(value = "name",required = false,defaultValue = "") String name){

        return regionService.search(pageIndex,pageSize,name);
    }


    /**
     * 添加区域
     * @param regionReq
     * @return
     */
    @PostMapping
    public boolean createRegion(@RequestBody RegionReq regionReq){
        RegionEntity region=new RegionEntity();
        region.setName(regionReq.getRegionName());
        region.setRemark(regionReq.getRemark());
        return regionService.createRegion(region);
    }


    /**
     * 修改区域
     * @param regionReq
     * @return
     */
    @PutMapping("/{id}")
    public boolean updateRegion(@PathVariable(name = "id")Long regionId,
                                @RequestBody RegionReq regionReq){
        return regionService.updateRegion(regionId,regionReq);
    }


    /**
     * 删除区域
     * @param regionId
     * @return
     */
    @DeleteMapping("/{id}")
    public boolean delRegion(@PathVariable(name = "id")Long regionId){
        return regionService.delRegion(regionId);
    }

}