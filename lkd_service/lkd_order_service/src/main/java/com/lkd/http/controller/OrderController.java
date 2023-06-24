package com.lkd.http.controller;

import com.github.wxpay.plus.WXConfig;
import com.github.wxpay.plus.WxPayTemplate;
import com.lkd.http.controller.vo.OrderVO;
import com.lkd.http.controller.vo.Pager;
import com.lkd.http.controller.vo.PayVO;
import com.lkd.service.ESOrderService;
import com.lkd.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    //创建订单
    @PostMapping("/weixinPay")
    public Map<String, String> weixinPay(@RequestBody PayVO payVO) {
        return orderService.weixinPay(payVO);
    }

    @Autowired
    private WxPayTemplate wxPayTemplate;

    // 微信支付回调接口
    @RequestMapping("/payNotify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 1.接收微信支付通知
        Map<String, String> resultMap = wxPayTemplate.validPay(request.getInputStream());
        // 接收失败了
        if (resultMap.get("code").equals("FAIL")) {
            log.error("接收微信通知消息失败...");
            return;
        }
        // 2.调用service处理业务
        orderService.payNotify(resultMap);
        // 3.返回业务处理结果
        response.setContentType("text/xml");
        response.getWriter().write(WXConfig.RESULT);

    }
    @Autowired
    private ESOrderService esOrderService;

    // 订单查询
    @GetMapping("/search")
    public Pager<OrderVO> search(
            @RequestParam(value = "pageIndex", required = false, defaultValue = "1") Integer pageIndex,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            String orderNo,
            String openId,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) throws IOException {
        return esOrderService.search(pageIndex, pageSize, orderNo, openId, startDate, endDate);
    }

}