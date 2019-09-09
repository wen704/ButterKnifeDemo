package com.gaofu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Gaofu
 * Time 2019-09-06 14:16
 */
@Target(ElementType.METHOD)// 该注解作用在方法之上
@Retention(RetentionPolicy.CLASS)// 编译器工作,通过注解处理器
public @interface OnClick {

    // 返回 R.id.xx
    int value();

}
