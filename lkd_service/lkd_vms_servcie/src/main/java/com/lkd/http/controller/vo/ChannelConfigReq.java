package com.lkd.http.controller.vo;

import com.lkd.entity.ChannelEntity;
import lombok.Data;

import java.util.List;

@Data
public class ChannelConfigReq {

    private String innerCode; // 售货机编号

    private List<ChannelEntity> channelList; //货道信息
}
