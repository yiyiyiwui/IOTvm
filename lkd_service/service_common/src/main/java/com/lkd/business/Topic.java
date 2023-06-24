package com.lkd.business;

import java.lang.annotation.*;

// 自定义注解
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Topic {


    String value();
}
