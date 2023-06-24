package com.lkd.emq;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MqttCallback implements MqttCallbackExtended {


    //需要订阅的topic配置
    @Value("${mqtt.consumer.consumerTopics}")
    private List<String> consumerTopics;



    @Autowired
    private MqttService mqttService;

    //当与服务器的连接丢失时调用此方法。
    @Override
    public void connectionLost(Throwable throwable) {
        log.error("emq error.",throwable);
    }

    //当消息从服务器到达时调用此方法
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info( "topic:"+topic+"  message:"+ new String(message.getPayload())   );
        
        //处理消息--在此处编写业务代码
        mqttService.processMessage(topic, message);

        //处理成功后确认消息
        mqttClient.messageArrivedComplete(message.getId(),message.getQos());
    }


    //当消息的传递完成并收到所有确认时调用。
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        log.info("deliveryComplete---------" + iMqttDeliveryToken.isComplete());
    }


    // 当与服务器的连接成功完成时调用该方法。
    @Override
    public void connectComplete(boolean b, String s) {
        //和EMQ连接成功后根据配置自动订阅topic
        if(consumerTopics != null && consumerTopics.size() > 0){
            consumerTopics.forEach(t->{
                try {
                        log.info(">>>>>>>>>>>>>>subscribe topic:"+t);
                        mqttClient.subscribe(t, 2);
                    } catch (MqttException e) {
                        log.error("emq connect error", e);
                    }
            });
        }
    }


    // 声明client客户端
    private MqttClient mqttClient;

    // 提供set方法设置client客户端
    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }
}