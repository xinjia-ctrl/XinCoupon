package com.xinjia.coupon.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.xinjia.coupon.common.sharding.ShardingProperties;
import com.xinjia.coupon.search.sync.SearchSyncProperties;
import com.xinjia.coupon.user.coupon.infrastructure.rocketmq.RocketMqCouponReceiveProperties;

@Configuration
@EnableConfigurationProperties({
        ShardingProperties.class,
        SearchSyncProperties.class,
        RocketMqCouponReceiveProperties.class
})
public class ShardingConfig {
}
