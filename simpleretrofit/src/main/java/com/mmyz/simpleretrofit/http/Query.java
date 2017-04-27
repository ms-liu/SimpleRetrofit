package com.mmyz.simpleretrofit.http;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ==============================================
 * <p>
 * 类名：添加查询参数注解
 * <p>
 * 作者：M-Liu
 * <p>
 * 时间：2017/4/16
 * <p>
 * 邮箱：ms_liu163@163.com
 * <p>
 * ==============================================
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    String value()default "";

    boolean encoded()default false;
}
