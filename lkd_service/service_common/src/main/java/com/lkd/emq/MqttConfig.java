package com.lkd.emq;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
@Data
@Slf4j
public class MqttConfig {
    
    @Value("${mqtt.client.username}")
    private String username;
    @Value("${mqtt.client.password}")
    private String password;
    @Value("${mqtt.client.serverURI}")
    private String serverURI;
    @Value("${mqtt.client.clientId}")
    private String clientId;
    @Value("${mqtt.client.keepAliveInterval}")
    private int keepAliveInterval;
    @Value("${mqtt.client.connectionTimeout}")
    private int connectionTimeout;


    @Autowired
    private MqttCallback mqttCallback;

	/*创建MQTT客户端*/
    @Bean
    public MqttClient mqttClient() {
        try {
             //新建客户端 参数：MQTT服务的地址，客户端名称，持久化
            MqttClient client = new MqttClient(serverURI, clientId, mqttClientPersistence());
            //设置手动消息接收确认
            client.setManualAcks(true);
            // 设置回调类（主要用于订阅者客户端接收消息时使用）
            client.setCallback(mqttCallback);
            mqttCallback.setMqttClient(client);
            // 设置连接的配置
            client.connect(mqttConnectOptions());
            return client;
        } catch (MqttException e) {
            log.error("emq connect error",e);
            return null;
        }
    }

    /*创建MQTT配置类*/
    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);//是否自动重新连接
        options.setCleanSession(true);//是否清除之前的连接信息
        options.setConnectionTimeout(connectionTimeout);//连接超时时间
        options.setKeepAliveInterval(keepAliveInterval);//心跳
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);//设置mqtt版本
        return options;
    }

    /*设置持久化*/
    public MqttClientPersistence mqttClientPersistence() {
        return new MemoryPersistence();
    }

}