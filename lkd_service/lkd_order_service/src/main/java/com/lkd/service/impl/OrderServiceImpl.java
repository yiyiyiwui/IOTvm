package com.lkd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.plus.WxPayParam;
import com.github.wxpay.plus.WxPayTemplate;
import com.lkd.common.VMSystem;
import com.lkd.config.TopicConfig;
import com.lkd.contract.OrderCheck;
import com.lkd.contract.VendoutContract;
import com.lkd.contract.VendoutData;
import com.lkd.contract.VendoutResultContract;
import com.lkd.dao.OrderDao;
import com.lkd.emq.MqttProducer;
import com.lkd.entity.OrderEntity;
import com.lkd.exception.LogicException;
import com.lkd.feign.UserServiceFeignClient;
import com.lkd.feign.VmServiceFeignClient;
import com.lkd.http.controller.vo.*;
import com.lkd.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private VmServiceFeignClient vmServiceFeignClient;

    @Autowired
    private UserServiceFeignClient userServiceFeignClient;

    @Autowired
    private WxPayTemplate wxPayTemplate;

    @Autowired
    private MqttProducer producer;

    @Autowired
    private StringRedisTemplate redisTemplate;

    //创建订单
    @Override
    @Transactional
    public Map<String, String> weixinPay(PayVO payVO) {
        // 1.参数校验 innerCode、skuId
        if (StrUtil.isEmpty(payVO.getInnerCode()) || StrUtil.isEmpty(payVO.getSkuId())) {
            throw new LogicException("参数不完整");
        }
        // 2.业务校验
        // 2-1 售货机是否存在，是否为运营状态
        VmVO vmVO = vmServiceFeignClient.getInnerCode(payVO.getInnerCode());
        if (vmVO == null) {
            throw new LogicException("此售货机不存在");
        }
        if (!vmVO.getVmStatus().equals(VMSystem.VM_STATUS_RUNNING)) {
            throw new LogicException("此售货机非运营");
        }
        // 2-2 货道库存是否有货，商品是否存在
        ChannelVO channelVO = vmServiceFeignClient.hasCapacity(payVO.getInnerCode(), Long.parseLong(payVO.getSkuId()));
        if (channelVO == null) {
            throw new LogicException("没货...");
        }
        // ------------------------校验redis库存
        String key = VMSystem.getSkuInventoryKey(payVO.getInnerCode(), Long.parseLong(payVO.getSkuId()));
        Long decrement = redisTemplate.opsForValue().decrement(key);
        if (decrement < 0) {
            // 注意库存至少为0
            redisTemplate.opsForValue().set(key, "0");
            // 拦截不在创建订单
            // throw new LogicException("没货...");
            log.error("没货....");
            return null;
        }
        // ------------------------校验redis库存

        SkuVO skuVO = channelVO.getSkuVO();
        // 3.创建订单
        OrderEntity orderEntity = new OrderEntity();
        // 复制skuVo到订单
        BeanUtil.copyProperties(skuVO, orderEntity);
        // 复制vmVo到订单
        BeanUtil.copyProperties(vmVO, orderEntity);
        orderEntity.setOrderNo(payVO.getInnerCode() + System.nanoTime() + ""); // 订单号
        orderEntity.setThirdNo(orderEntity.getOrderNo()); // 第三方订单号
        orderEntity.setInnerCode(payVO.getInnerCode()); // 设备号
        orderEntity.setStatus(VMSystem.ORDER_STATUS_CREATE); // 订单状态
        orderEntity.setAmount(skuVO.getRealPrice());// 支付金额
        orderEntity.setPayType("2"); // 微信支付
        orderEntity.setPayStatus(VMSystem.PAY_STATUS_NOPAY); // 未支付
        // 合作商
        PartnerVO partnerVO = userServiceFeignClient.getPartner(vmVO.getOwnerId());
        BigDecimal bg = new BigDecimal(skuVO.getRealPrice());
        int bill = bg.multiply(new
                BigDecimal(partnerVO.getRatio())).divide(new BigDecimal(100), 0,
                RoundingMode.HALF_UP).intValue();
        orderEntity.setBill(bill);
        orderEntity.setAddr(vmVO.getNodeAddr()); // 点位地址
        orderEntity.setOpenId(payVO.getOpenId()); // 支付用户id

        this.save(orderEntity);

        // 4.调用第三方微信支付
        // 支付参数
        WxPayParam wxPayParam = new WxPayParam();
        wxPayParam.setBody(skuVO.getSkuName()); // 商品名
        wxPayParam.setOutTradeNo(orderEntity.getOrderNo()); // 订单号
        wxPayParam.setTotalFee(1); // TODO 写死 1分钱
        wxPayParam.setDuration(600L); // 10分钟
        wxPayParam.setOpenid(orderEntity.getOpenId());
        Map<String, String> wxMap = wxPayTemplate.requestPay(wxPayParam);

        // 5.预留位置，延迟消息
        // 内容
        OrderCheck orderCheck = new OrderCheck();
        orderCheck.setInnerCode(orderEntity.getInnerCode());
        orderCheck.setOrderNo(orderEntity.getOrderNo());
        String json = JSONUtil.toJsonStr(orderCheck);
        // 主题
        String topic = "$delayed/60/" + TopicConfig.ORDER_CHECK_TOPIC;
        producer.send(topic, json);
        log.info("发送mq消息");
        return wxMap;
    }

    // 处理超时订单
    @Override
    public void handlerTimeOutOrder(OrderCheck orderCheck) {
        // 先查询订单
        OrderEntity order = this.lambdaQuery()
                .eq(OrderEntity::getOrderNo, orderCheck.getOrderNo())
                .one();
        if (order == null) {
            log.info("此订单不存在：{}", orderCheck.getOrderNo());
        }
        // 检查一遍订单状态
        if (!order.getStatus().equals(VMSystem.ORDER_STATUS_CREATE)) {
            log.info("此订单不需要取消：{}", orderCheck.getOrderNo());
        }

        // 更新订单状态
        // update tb_order set status=4 and update_time = now() where order_no=? and status
        this.lambdaUpdate()
                .set(OrderEntity::getStatus, VMSystem.ORDER_STATUS_INVALID)
                .set(OrderEntity::getUpdateTime, LocalDateTime.now())
                .eq(OrderEntity::getOrderNo, orderCheck.getOrderNo())
                .eq(OrderEntity::getStatus, VMSystem.ORDER_STATUS_CREATE)
                .update();

        // ----------------------redis恢复库存 +1
        String key = VMSystem.getSkuInventoryKey(order.getInnerCode(), order.getSkuId());
        redisTemplate.opsForValue().increment(key);
        // ----------------------redis恢复库存 +1
    }

    @Autowired
    private MqttProducer mqttProducer;

    // 支付通知处理
    @Override
    public void payNotify(Map<String, String> resultMap) {
        // 1.业务校验
        String orderSn = resultMap.get("order_sn");
        OrderEntity orderEntity = this.lambdaQuery()
                .eq(OrderEntity::getOrderNo, orderSn)
                .one();
        if (orderEntity == null) {
            throw new LogicException("此订单不存在");
        }
        if (!orderEntity.getStatus().equals(VMSystem.ORDER_STATUS_CREATE)) {
            throw new LogicException("此订单状态不正确");
        }
        // 2.修改订单信息
        String openId = resultMap.get("openId");
        orderEntity.setOpenId(openId); // 付款用户
        orderEntity.setPayStatus(VMSystem.PAY_STATUS_PAYED); // 支付状态
        orderEntity.setStatus(VMSystem.ORDER_STATUS_PAYED); // 订单状态
        orderEntity.setUpdateTime(LocalDateTime.now());
        this.updateById(orderEntity);

        // ---- 发送mq出货消息
        VendoutContract vendoutContract = new VendoutContract();
        vendoutContract.setInnerCode(orderEntity.getInnerCode());
        VendoutData vendoutData = new VendoutData();
        vendoutData.setSkuId(orderEntity.getSkuId());
        vendoutData.setOrderNo(orderEntity.getOrderNo());
        vendoutContract.setVendoutData(vendoutData);
        String json = JSONUtil.toJsonStr(vendoutContract);
        mqttProducer.send(TopicConfig.VMS_VENDOUT_TOPIC, json);
        log.info("订单微服务发送出货消息：{}", json);

    }

    // 订单微服务处理出货结果
    @Override
    public Boolean vendoutResult(VendoutResultContract vendoutContract) {
        // 查询订单数据
        OrderEntity orderEntity = this.lambdaQuery()
                .eq(OrderEntity::getOrderNo, vendoutContract.getVendoutData().getOrderNo())
                .one();

        // 1.判断设备是否成功
        if (vendoutContract.isSuccess() == true) { // 表示成功
            orderEntity.setStatus(VMSystem.ORDER_STATUS_VENDOUT_SUCCESS);
        } else { // 表示失败
            orderEntity.setStatus(VMSystem.ORDER_STATUS_VENDOUT_FAIL);
            // TODO 订单退款
        }
        return this.updateById(orderEntity);
    }
}
