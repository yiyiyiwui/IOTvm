package com.lkd.business;


// 策略上下文接口
public interface MsgHandlerContext {

    MsgHandler getMsgHandler(String topic);
}
