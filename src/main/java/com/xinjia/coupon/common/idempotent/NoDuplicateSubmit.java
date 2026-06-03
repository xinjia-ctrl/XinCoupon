package com.xinjia.coupon.common.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoDuplicateSubmit {

    String key();

    long ttlSeconds() default 5;

    String message() default "请勿重复提交";
}
