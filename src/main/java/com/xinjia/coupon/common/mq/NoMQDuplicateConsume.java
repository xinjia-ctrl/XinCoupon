package com.xinjia.coupon.common.mq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoMQDuplicateConsume {

    String key();

    long consumingTtlSeconds() default 120;

    long consumedTtlSeconds() default 604800;
}
