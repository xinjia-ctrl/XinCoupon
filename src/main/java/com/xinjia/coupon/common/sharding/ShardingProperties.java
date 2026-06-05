package com.xinjia.coupon.common.sharding;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xincoupon.sharding")
public class ShardingProperties {

    private boolean enabled = false;
    private ShardingMode mode = ShardingMode.MANUAL;
    private int userCouponTableCount = 2;
    private int couponBatchTaskTableCount = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ShardingMode getMode() {
        return mode;
    }

    public void setMode(ShardingMode mode) {
        this.mode = mode;
    }

    public boolean isManualEnabled() {
        return enabled && mode == ShardingMode.MANUAL;
    }

    public boolean isShardingSphereEnabled() {
        return enabled && mode == ShardingMode.SHARDINGSPHERE;
    }

    public int getUserCouponTableCount() {
        return userCouponTableCount;
    }

    public void setUserCouponTableCount(int userCouponTableCount) {
        this.userCouponTableCount = userCouponTableCount;
    }

    public int getCouponBatchTaskTableCount() {
        return couponBatchTaskTableCount;
    }

    public void setCouponBatchTaskTableCount(int couponBatchTaskTableCount) {
        this.couponBatchTaskTableCount = couponBatchTaskTableCount;
    }

    public enum ShardingMode {
        MANUAL,
        SHARDINGSPHERE
    }
}
