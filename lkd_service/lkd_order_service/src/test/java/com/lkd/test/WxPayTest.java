package com.lkd.test;

import com.github.wxpay.plus.WxPayParam;
import com.github.wxpay.plus.WxPayTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootTest
public class WxPayTest {

    @Autowired
    private WxPayTemplate wxPayTemplate;

    @Test
    public void test() throws Exception {
        // 支付参数
        WxPayParam wxPayParam = new WxPayParam();
        wxPayParam.setBody("可口可乐");
        wxPayParam.setOutTradeNo(System.nanoTime()+""); // 订单号
        wxPayParam.setTotalFee(1); // 1分钱
        wxPayParam.setDuration(600L); // 10分钟

        // 生成微信支付链接
        Map<String, String> map = wxPayTemplate.requestPay(wxPayParam);
        System.out.println(map);
    }
}
