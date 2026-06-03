package com.xinjia.coupon.common.sharding;

import org.springframework.stereotype.Component;

@Component
public class ModuloCouponShardRouter implements CouponShardRouter {

    private static final String USER_COUPON_TABLE = "user_coupon";
    private static final String COUPON_BATCH_TASK_TABLE = "coupon_batch_task";

    private final ShardingProperties shardingProperties;

    public ModuloCouponShardRouter(ShardingProperties shardingProperties) {
        this.shardingProperties = shardingProperties;
    }

    @Override
    public ShardTarget routeUserCoupon(Long userId) {
        int shardIndex = routeByLong(userId, shardingProperties.getUserCouponTableCount());
        return new ShardTarget(USER_COUPON_TABLE, USER_COUPON_TABLE + "_" + shardIndex, shardIndex);
    }

    @Override
    public ShardTarget routeCouponBatchTask(String batchNo) {
        int shardIndex = routeByText(batchNo, shardingProperties.getCouponBatchTaskTableCount());
        return new ShardTarget(COUPON_BATCH_TASK_TABLE, COUPON_BATCH_TASK_TABLE + "_" + shardIndex, shardIndex);
    }

    private int routeByLong(Long shardingValue, int tableCount) {
        validateTableCount(tableCount);
        if (shardingValue == null || shardingValue <= 0) {
            throw new IllegalArgumentException("分片键必须为正数");
        }
        return Math.floorMod(shardingValue, tableCount);
    }

    private int routeByText(String shardingValue, int tableCount) {
        validateTableCount(tableCount);
        if (shardingValue == null || shardingValue.isBlank()) {
            throw new IllegalArgumentException("分片键不能为空");
        }
        return Math.floorMod(shardingValue.hashCode(), tableCount);
    }

    private void validateTableCount(int tableCount) {
        if (tableCount <= 0) {
            throw new IllegalArgumentException("分表数量必须大于 0");
        }
    }
}
