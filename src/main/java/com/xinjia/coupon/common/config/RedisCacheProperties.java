package com.xinjia.coupon.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "framework.cache.redis")
public class RedisCacheProperties {

    private String prefix = "xin-coupon:";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String buildKey(String key) {
        return prefix + key;
    }
}
