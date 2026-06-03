package com.xinjia.coupon.common.sharding;

public interface CouponShardRouter {

    ShardTarget routeUserCoupon(Long userId);

    ShardTarget routeCouponBatchTask(String batchNo);
}
