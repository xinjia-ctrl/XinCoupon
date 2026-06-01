package com.xinjia.coupon.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "framework.cache.redis")
public class RedisCacheProperties {

    private String uniqueName = "xin-coupon";
    private String prefix = "";

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String buildKey(String key) {
        return normalizedPrefix() + key;
    }

    private String normalizedPrefix() {
        String rawPrefix = hasText(prefix) ? prefix : uniqueName;
        if (!hasText(rawPrefix)) {
            rawPrefix = "xin-coupon";
        }
        return rawPrefix.endsWith(":") ? rawPrefix : rawPrefix + ":";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
