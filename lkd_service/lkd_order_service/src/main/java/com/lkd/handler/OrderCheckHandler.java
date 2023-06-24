package com.lkd.handler;

import cn.hutool.json.JSONUtil;
import com.lkd.business.MsgHandler;
import com.lkd.business.Topic;
import com.lkd.config.TopicConfig;
import com.lkd.contract.OrderCheck;
import com.lkd.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Topic(TopicConfig.ORDER_CHECK_TOPIC)
@Slf4j
public class OrderCheckHandler implements MsgHandler {

    @Autowired
    private OrderService orderService;

    // 接收延迟消息
    @Override
    public void process(String jsonMsg) {
        log.info("接收延迟订单消息：{}", jsonMsg);

        OrderCheck orderCheck = JSONUtil.toBean(jsonMsg, OrderCheck.class);
        orderService.handlerTimeOutOrder(orderCheck);
    }
}
