package com.lkd.test;

import com.lkd.sms.SmsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class SmsTemplateTest {
    @Autowired
    private SmsTemplate smsTemplate;

    @Test
    public void testSendSMS(){
        smsTemplate.sendSms("18132329190", "123456");
    }
}