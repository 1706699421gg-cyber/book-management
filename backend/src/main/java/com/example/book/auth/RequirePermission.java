package com.example.book.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限注解: 标注在 Controller 方法或类上, 由 AuthInterceptor 统一校验
 * 多个权限编码为"全部满足"语义
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /** 需要的权限编码, 例如 book:add */
    String[] value();
}
