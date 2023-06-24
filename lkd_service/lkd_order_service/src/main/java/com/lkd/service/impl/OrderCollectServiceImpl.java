package com.lkd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lkd.dao.OrderCollectDao;
import com.lkd.entity.OrderCollectEntity;
import com.lkd.service.OrderCollectService;
import org.springframework.stereotype.Service;

@Service
public class OrderCollectServiceImpl extends ServiceImpl<OrderCollectDao, OrderCollectEntity> implements OrderCollectService {

}
