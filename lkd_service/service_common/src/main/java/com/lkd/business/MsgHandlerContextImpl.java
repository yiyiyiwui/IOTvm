package com.lkd.business;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// 上下文实现获取指定topic策略
@Component
public class MsgHandlerContextImpl implements MsgHandlerContext, ApplicationContextAware {

    private Map<String, MsgHandler> handlerMap = new HashMap<>();

    @Override
    public MsgHandler getMsgHandler(String topic) {
        return handlerMap.get(topic);
    }

    // 从ioc容器中获取指定类型对象
    @Override
    public void setApplicationContext(ApplicationContext ioc) throws BeansException {
        // 获取对象
        Map<String, MsgHandler> beanMap = ioc.getBeansOfType(MsgHandler.class);
        for (MsgHandler msgHandler : beanMap.values()) {
            // 获取topic注解的值
            String topic = msgHandler.getClass().getAnnotation(Topic.class).value();
            // 封装到map
            handlerMap.put(topic, msgHandler);
        }
    }
}
