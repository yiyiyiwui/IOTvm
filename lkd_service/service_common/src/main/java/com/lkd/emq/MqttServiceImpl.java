package com.lkd.emq;

import com.lkd.business.MsgHandler;
import com.lkd.business.MsgHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息分发处理器
 */
@Component
@Slf4j
public class MqttServiceImpl implements MqttService {




    @Autowired
    private MsgHandlerContext msgHandlerContext;
    /**
     * mqtt消息处理
     *
     * @param topic
     * @param message
     */
    @Override
    public void processMessage(String topic, MqttMessage message) {
        String msgContent = new String(message.getPayload());
        log.info("接收到消息:" + msgContent);
        //执行业务。。。开始
        MsgHandler msgHandler = msgHandlerContext.getMsgHandler(topic);
        if (msgHandler==null) {
            return;
        }
        msgHandler.process(msgContent);
        //执行业务。。。结束
    }
}