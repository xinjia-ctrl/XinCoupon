package com.xinjia.coupon.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.xinjia.coupon.common.sharding.ShardingProperties;

@Configuration
@EnableConfigurationProperties(ShardingProperties.class)
public class ShardingConfig {
}
