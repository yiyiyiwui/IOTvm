package com.lkd.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lkd.common.VMSystem;
import com.lkd.config.TopicConfig;
import com.lkd.contract.*;
import com.lkd.dao.VendingMachineDao;
import com.lkd.emq.MqttProducer;
import com.lkd.entity.*;
import com.lkd.http.controller.vo.Pager;
import com.lkd.http.controller.vo.SkuVO;
import com.lkd.http.controller.vo.VmVO;
import com.lkd.service.*;
import com.lkd.utils.UUIDUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class VendingMachineServiceImpl extends ServiceImpl<VendingMachineDao, VendingMachineEntity> implements VendingMachineService {

    @Override
    public Pager<VendingMachineEntity> search(Integer pageIndex, Integer pageSize, Integer status, String innerCode) {
        // 1.查询售货机基本信息
        // 1-1 分页
        Page<VendingMachineEntity> page = new Page<>(pageIndex, pageSize);
        // 1-2 条件
        LambdaQueryWrapper<VendingMachineEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(ObjectUtil.isNotEmpty(status), VendingMachineEntity::getVmStatus, status)
                .eq(StrUtil.isNotEmpty(innerCode), VendingMachineEntity::getInnerCode, innerCode);
        // 1-3 查询
        this.page(page, queryWrapper);
        // 改成mp自动映射

        return Pager.build(page);
    }

    @Autowired
    private NodeService nodeService;

    @Autowired
    private VmTypeService vmTypeService;

    @Autowired
    private ChannelService channelService;

    // 新增售货机
    @Override
    @Transactional
    public Boolean addVM(VendingMachineEntity vendingMachineEntity) {
        // 1.保存售货机基本信息
        // 1-1 补充信息
        String innerCode = UUIDUtils.getUUID();
        vendingMachineEntity.setInnerCode(innerCode);
        vendingMachineEntity.setVmStatus(VMSystem.VM_STATUS_NODEPLOY);
        // 查询点位
        NodeEntity nodeEntity = nodeService.getById(vendingMachineEntity.getNodeId());
        BeanUtil.copyProperties(nodeEntity, vendingMachineEntity);
        vendingMachineEntity.setClientId(UUIDUtils.generateClientId(innerCode));
        // 1-2 保存
        this.save(vendingMachineEntity);

        // 2.关联保存该售货机货道信息
        // 2-1 先查询售货机类型
        VmTypeEntity vmTypeEntity = vmTypeService.getById(vendingMachineEntity.getVmType());
        // 2-2 双重for循环  行+列
        List<ChannelEntity> channelList = new ArrayList<>();
        for (int i = 1; i <= vmTypeEntity.getVmRow(); i++) {
            for (int j = 1; j <= vmTypeEntity.getVmCol(); j++) {
                // 创建货道对象
                ChannelEntity channelEntity = new ChannelEntity();
                channelEntity.setChannelCode(i + "-" + j);
                channelEntity.setVmId(vendingMachineEntity.getId());
                channelEntity.setInnerCode(vendingMachineEntity.getInnerCode());
                channelEntity.setMaxCapacity(vmTypeEntity.getChannelMaxCapacity());
                // 保存
                // channelService.save(channelEntity);
                channelList.add(channelEntity);
            }
        }
        // 批量保存
        return channelService.saveBatch(channelList);
    }

    // 根据设备编号，查询VmVo
    @Override
    public VmVO getInnerCode(String innerCode) {
        // 1.构建条件
        /*LambdaQueryWrapper<VendingMachineEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(VendingMachineEntity::getInnerCode, innerCode);
        this.getOne(lambdaQueryWrapper);*/
        VendingMachineEntity vm = this.lambdaQuery()
                .eq(VendingMachineEntity::getInnerCode, innerCode)
                .one();
        // 2.实体转vo
        VmVO vmVO = BeanUtil.copyProperties(vm, VmVO.class);
        // 3.补充点位信息
        NodeEntity node = nodeService.getById(vm.getNodeId());
        vmVO.setNodeName(node.getName());
        vmVO.setNodeAddr(node.getAddr());
        // 4.返回结果
        return vmVO;
    }

    // 根据工单类型，修改设备状态
    @Override
    public Boolean updateVMStatus(TaskCompleteContract taskCompleteContract) {
        // sql语句： update tb_vending_machine set vm_status=?  where inner_code= ?
        LambdaUpdateWrapper<VendingMachineEntity> uw = new LambdaUpdateWrapper<>();
        uw.eq(VendingMachineEntity::getInnerCode, taskCompleteContract.getInnerCode()); // 条件
        // 判断根据工单类型设置不同设备状态
        if (taskCompleteContract.getTaskType() == VMSystem.TASK_TYPE_DEPLOY) {
            // 投放 -> 运营
            uw.set(VendingMachineEntity::getVmStatus, VMSystem.VM_STATUS_RUNNING);
        } else if (taskCompleteContract.getTaskType() == VMSystem.TASK_TYPE_REVOKE) {
            // 撤机 -> 撤机
            uw.set(VendingMachineEntity::getVmStatus, VMSystem.VM_STATUS_REVOKE);
        }
        // 更新数据库
        return this.update(uw);
    }

    // 运营补货代码
    @Override
    @Transactional
    public Boolean supply(SupplyContract supplyContract) {
        // 货道补货
        // update tb_channel set current_capacity= current_capacity+? where inner_code=? and channel_code = ?
        for (SupplyChannel supplyChannel : supplyContract.getSupplyData()) {
            LambdaUpdateWrapper<ChannelEntity> uw = new LambdaUpdateWrapper<>();
            uw.eq(ChannelEntity::getInnerCode, supplyContract.getInnerCode()); // 条件1 售货机编号
            uw.eq(ChannelEntity::getChannelCode, supplyChannel.getChannelId()); // 条件2 货道号
            uw.setSql("current_capacity= current_capacity+" + supplyChannel.getCapacity());// 补货数据
            uw.set(ChannelEntity::getLastSupplyTime, LocalDateTime.now()); // 最后补货时间
            channelService.update(uw);

            // 将补货同步到redis
            String key = VMSystem.getSkuInventoryKey(supplyContract.getInnerCode(), supplyChannel.getSkuId());
            redisTemplate.opsForValue().increment(key, supplyChannel.getCapacity());

        }
        // 更新售货机补货时间
        // update tb_vending_machine set last_supply_time=?  where inner_code= ?
        LambdaUpdateWrapper<VendingMachineEntity> vuw = new LambdaUpdateWrapper<>();
        vuw.eq(VendingMachineEntity::getInnerCode, supplyContract.getInnerCode());
        vuw.set(VendingMachineEntity::getLastSupplyTime, LocalDateTime.now());
        this.update(vuw);
        return true;
    }

    @Autowired
    private SkuService skuService;

    @Autowired
    private MqttProducer producer;

    @Override
    public void autoSupplyTask() {

        // 1.查询所有运营中设备列表
        List<VendingMachineEntity> vmList = this.lambdaQuery()
                .eq(VendingMachineEntity::getVmStatus, VMSystem.VM_STATUS_RUNNING)
                .list();

        // 2.遍历设备
        for (VendingMachineEntity vm : vmList) {

            // 准备补货对象
            SupplyContract supplyContract = new SupplyContract();
            supplyContract.setInnerCode(vm.getInnerCode());
            // 声明补货list
            List<SupplyChannel> supplyData = new ArrayList<>();
            supplyContract.setSupplyData(supplyData);

            // 3.查询货道
            List<ChannelEntity> channelList = channelService.lambdaQuery().eq(ChannelEntity::getInnerCode, vm.getInnerCode()).list();
            // 4.遍历货道
            for (ChannelEntity channel : channelList) {
                // 5.判断 当前容量 < 最大容量，且 此货道关联商品
                if (channel.getCurrentCapacity() < channel.getMaxCapacity() && channel.getSkuId() != 0) {
                    // 需要补货
                    SupplyChannel sc = new SupplyChannel();
                    sc.setChannelId(channel.getChannelCode()); // 货道编号
                    sc.setCapacity(channel.getMaxCapacity() - channel.getCurrentCapacity()); // 补货数量
                    sc.setSkuId(channel.getSkuId()); // 商品id
                    // 查询商品对象
                    SkuEntity sku = skuService.getById(channel.getSkuId());
                    sc.setSkuName(sku.getSkuName()); // 商品名称
                    sc.setSkuImage(sku.getSkuImage()); // 商品图片
                    // 添加到补货集合
                    supplyData.add(sc);
                }
            }

            // 发送当前设备mq补货消息
            if (supplyData.size() > 0) {
                log.info("发送了补货消息：{}" + vm.getInnerCode());
                try {
                    producer.send(TopicConfig.TASK_SUPPLY_TOPIC, JSONUtil.toJsonStr(supplyContract));
                } catch (Exception e) {
                    log.info("发送补货消息失败：{}", vm.getInnerCode());
                }
            }
        }
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void autoSupplyTask(int total, int index) {
        // 1.查询所有运营中设备列表
        // SELECT * FROM `tb_vending_machine` WHERE MOD(id,3)=2
        List<VendingMachineEntity> vmList = this.lambdaQuery()
                .eq(VendingMachineEntity::getVmStatus, VMSystem.VM_STATUS_RUNNING)
                .apply("mod(id," + total + ")=" + index)
                .list();

        log.info("片：{},查询了：{}", index, vmList.size());
        // 2.遍历设备
        for (VendingMachineEntity vm : vmList) {

            // 准备补货对象
            SupplyContract supplyContract = new SupplyContract();
            supplyContract.setInnerCode(vm.getInnerCode());
            // 声明补货list
            List<SupplyChannel> supplyData = new ArrayList<>();
            supplyContract.setSupplyData(supplyData);

            // 3.查询货道
            List<ChannelEntity> channelList = channelService.lambdaQuery().eq(ChannelEntity::getInnerCode, vm.getInnerCode()).list();
            // 4.遍历货道
            for (ChannelEntity channel : channelList) {
                // 5.判断 当前容量 < 最大容量，且 此货道关联商品
                if (channel.getCurrentCapacity() < channel.getMaxCapacity() && channel.getSkuId() != 0) {
                    // 需要补货
                    SupplyChannel sc = new SupplyChannel();
                    sc.setChannelId(channel.getChannelCode()); // 货道编号
                    sc.setCapacity(channel.getMaxCapacity() - channel.getCurrentCapacity()); // 补货数量
                    sc.setSkuId(channel.getSkuId()); // 商品id
                    // 查询商品对象
                    SkuEntity sku = skuService.getById(channel.getSkuId());
                    sc.setSkuName(sku.getSkuName()); // 商品名称
                    sc.setSkuImage(sku.getSkuImage()); // 商品图片
                    // 添加到补货集合
                    supplyData.add(sc);
                }

                if (channel.getSkuId() != 0 && channel.getMaxCapacity() >= 0) {
                    // 将数据初始化到redis
                    String key = VMSystem.getSkuInventoryKey(channel.getInnerCode(), channel.getSkuId());
                    // 判断redis是否有此数据
                    if (!redisTemplate.hasKey(key)) {
                        // 如果没有进行初始化
                        redisTemplate.opsForValue().set(key, channel.getCurrentCapacity().toString());
                    }
                }
            }

            // 发送当前设备mq补货消息
            if (supplyData.size() > 0) {
                log.info("发送了补货消息：{}" + vm.getInnerCode());
                try {
                    producer.send(TopicConfig.TASK_SUPPLY_TOPIC, JSONUtil.toJsonStr(supplyContract));
                } catch (Exception e) {
                    log.info("发送补货消息失败：{}", vm.getInnerCode());
                }
            }
        }
    }

    // 获取售货机商品列表
    @Override
    public List<SkuVO> getSkuListByInnerCode(String innerCode) {
        // 1.根据设备编号和skuId>0查询货道列表
        List<ChannelEntity> channelList = channelService.lambdaQuery()
                .eq(ChannelEntity::getInnerCode, innerCode)
                .gt(ChannelEntity::getSkuId, 0)
                .list();
        // 2.遍历货道
        List<SkuVO> voList = new ArrayList<>();

        for (ChannelEntity channel : channelList) {
            // 查询商品信息
            SkuEntity sku = skuService.getById(channel.getSkuId());
            // 封装skuVO
            SkuVO skuVO = BeanUtil.copyProperties(sku, SkuVO.class);
            skuVO.setImage(sku.getSkuImage()); // 图片
            skuVO.setRealPrice(sku.getPrice()); // 真实售价
            skuVO.setCapacity(channel.getCurrentCapacity()); // 商品余量
            // 添加到集合
            voList.add(skuVO);
        }
        return voList;
    }

    @Autowired
    private VendoutRunningService vendoutRunningService;

    @Autowired
    private MqttProducer mqttProducer;

    // 处理出货逻辑
    @Override
    @Transactional
    public Boolean vendout(VendoutContract vendoutContract) {
        // 1.业务校验
        // 1-1 查询设备是否存在
        VmVO vmVO = this.getInnerCode(vendoutContract.getInnerCode());
        if (vmVO == null) {
            log.error("此设备不存在：{}", vendoutContract.getInnerCode());
            return false;
        }
        // 1-2 设备是否处于运营状态
        if (!vmVO.getVmStatus().equals(VMSystem.VM_STATUS_RUNNING)) {
            log.error("此设备非运营：{}", vendoutContract.getInnerCode());
            return false;
        }
        // 1-3 查询货道
        List<ChannelEntity> channelList = channelService.lambdaQuery()
                .eq(ChannelEntity::getInnerCode, vendoutContract.getInnerCode())
                .eq(ChannelEntity::getSkuId, vendoutContract.getVendoutData().getSkuId())
                .gt(ChannelEntity::getCurrentCapacity, 0)
                .list();
        if (CollUtil.isEmpty(channelList)) {
            log.error("此设备商品无货：", vendoutContract.getInnerCode());
            return false;
        }
        // 2.货道扣库存 （当前库存使用了 无符号int 不能为负数）
        ChannelEntity channelEntity = channelList.stream().findFirst().get();
        channelEntity.setCurrentCapacity(channelEntity.getCurrentCapacity() - 1);
        try {
            channelService.updateById(channelEntity);
        } catch (Exception e) {
            log.error("库存不足：", vendoutContract.getInnerCode());
            e.printStackTrace();
            return false;
        }
        // 3.记录出货流水
        VendoutRunningEntity vendoutRunningEntity = new VendoutRunningEntity();
        vendoutRunningEntity.setOrderNo(vendoutContract.getVendoutData().getOrderNo());
        vendoutRunningEntity.setInnerCode(vendoutContract.getInnerCode());
        vendoutRunningEntity.setStatus(false); // 流水未最终完成
        SkuEntity skuEntity = skuService.getById(channelEntity.getSkuId());
        vendoutRunningEntity.setSkuId(skuEntity.getSkuId());
        vendoutRunningEntity.setSkuName(skuEntity.getSkuName());
        vendoutRunningEntity.setPrice(skuEntity.getPrice());
        vendoutRunningService.save(vendoutRunningEntity);

        // 4.发送mq出货消息给 给具体设备
        vendoutContract.getVendoutData().setChannelCode(channelEntity.getChannelCode()); // 设置具体货道
        String json = JSONUtil.toJsonStr(vendoutContract);
        String topic = TopicConfig.getVendoutTopic(vendoutContract.getInnerCode());
        mqttProducer.send(topic, json);
        log.info("售货机微服务发送出货消息：{}", vendoutContract.getInnerCode());
        return true;
    }

    // 售货机微服务处理出货结果
    @Override
    public Boolean vendoutResult(VendoutResultContract vendoutContract) {

        Boolean flag = false;

        // 判断出货成功还是失败
        if (vendoutContract.isSuccess() == true) {
            // 成功，更新出货流水状态
            flag = vendoutRunningService
                    .lambdaUpdate()
                    .set(VendoutRunningEntity::getStatus, true)
                    .set(VendoutRunningEntity::getUpdateTime, LocalDateTime.now())
                    .eq(VendoutRunningEntity::getOrderNo, vendoutContract.getVendoutData().getOrderNo())
                    .eq(VendoutRunningEntity::getSkuId, vendoutContract.getVendoutData().getSkuId())
                    .update();
        } else {
            // 失败，货道数量+1
            // update tb_channel set current_capacity = current_capacity+1 where inner_code=? and channel_code=?
            flag = channelService.lambdaUpdate()
                    .setSql("current_capacity = current_capacity+1")
                    .set(ChannelEntity::getUpdateTime, LocalDateTime.now())
                    .eq(ChannelEntity::getInnerCode, vendoutContract.getInnerCode())
                    .eq(ChannelEntity::getChannelCode, vendoutContract.getVendoutData().getChannelCode())
                    .update();

            // redis数量+1
            String key = VMSystem.getSkuInventoryKey(vendoutContract.getInnerCode(), vendoutContract.getVendoutData().getSkuId());
            redisTemplate.opsForValue().increment(key);
        }

        return flag;
    }
}
