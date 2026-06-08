package com.xinjia.coupon.distribution.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.xinjia.coupon")
public class CouponDistributionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponDistributionApplication.class, args);
    }
}
