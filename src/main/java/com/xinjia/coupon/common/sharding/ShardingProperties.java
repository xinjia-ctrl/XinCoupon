package com.xinjia.coupon.common.sharding;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xincoupon.sharding")
public class ShardingProperties {

    private boolean enabled = false;
    private int userCouponTableCount = 2;
    private int couponBatchTaskTableCount = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
}
