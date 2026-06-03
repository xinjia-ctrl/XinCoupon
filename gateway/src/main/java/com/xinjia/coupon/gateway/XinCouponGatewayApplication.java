package com.xinjia.coupon.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class XinCouponGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(XinCouponGatewayApplication.class, args);
    }
}
