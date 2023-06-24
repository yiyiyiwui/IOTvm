package com.lkd.emq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lkd.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MqttProducer {

    @Value("${mqtt.producer.defaultQos}")
    private int defaultProducerQos;
    @Value("${mqtt.producer.defaultRetained}")
    private boolean defaultRetained;
    @Value("${mqtt.producer.defaultTopic}")
    private String defaultTopic;

    @Autowired
    private MqttClient mqttClient;

    /**
     * 只发送消息的方法
     *
     * @param payload 消息信息
     */
    public void send(String payload) {
        this.send(defaultTopic, payload);
    }

    /**
     * 指定topic发送消息的方法
     *
     * @param topic   主体名称
     * @param payload 消息信息
     */
    public void send(String topic, String payload) {
        this.send(topic, defaultProducerQos, payload);
    }

    /**
     * 指定topic和服务质量QoS发送消息的方法
     *
     * @param topic   主体名称
     * @param qos     服务质量（0、1、2）
     * @param payload 消息信息
     */
    public void send(String topic, int qos, String payload) {
        this.send(topic, qos, defaultRetained, payload);
    }

    /**
     * 发送消息的全参方法
     *
     * @param topic    主体名称
     * @param qos      服务质量（0、1、2）
     * @param retained 是否保留消息
     * @param payload  消息信息
     */
    public void send(String topic, int qos, boolean retained, String payload) {
        try {
            mqttClient.publish(topic, payload.getBytes(), qos, retained);
        } catch (MqttException e) {
            log.error("publish msg error.", e);
        }
    }

    /**
     * 指定topic和服务质量QoS发送消息的方法
     *
     * @param topic 主体名称
     * @param qos   服务质量（0、1、2）
     * @param msg   转换为json类对象数据
     */
    public <T extends Object> void send(String topic, int qos, T msg) throws JsonProcessingException {
        String payload = JsonUtil.serialize(msg);
        this.send(topic, qos, payload);
    }
}
