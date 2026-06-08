package com.xinjia.coupon.engine.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.xinjia.coupon")
public class CouponEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponEngineApplication.class, args);
    }
}
