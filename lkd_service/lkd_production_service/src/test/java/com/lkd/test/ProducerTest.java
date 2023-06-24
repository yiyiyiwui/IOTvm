package com.lkd.test;

import com.lkd.emq.MqttProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProducerTest {

    @Autowired
    private MqttProducer mqttProducer;

    @Test
    public void test() throws Exception {
        mqttProducer.send("testtopic","{ \"msg\": \"Hello, World!\" }");
    }

}
