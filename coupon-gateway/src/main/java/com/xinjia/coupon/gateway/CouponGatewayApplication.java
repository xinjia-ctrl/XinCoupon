package com.xinjia.coupon.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.xinjia.coupon")
public class CouponGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponGatewayApplication.class, args);
    }
}
