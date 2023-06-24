package com.lkd.business;

// 策略接口
public interface MsgHandler {

    void process(String jsonMsg);
}
