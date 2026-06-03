package com.xinjia.coupon.common.sharding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ModuloCouponShardRouterTests {

    @Test
    void routeUserCouponShouldUseUserIdModulo() {
        ShardingProperties properties = new ShardingProperties();
        properties.setUserCouponTableCount(4);
        ModuloCouponShardRouter router = new ModuloCouponShardRouter(properties);

        ShardTarget target = router.routeUserCoupon(1005L);

        assertThat(target.logicalTable()).isEqualTo("user_coupon");
        assertThat(target.actualTable()).isEqualTo("user_coupon_1");
        assertThat(target.shardIndex()).isEqualTo(1);
    }

    @Test
    void routeCouponBatchTaskShouldBeStableForSameBatchNo() {
        ShardingProperties properties = new ShardingProperties();
        properties.setCouponBatchTaskTableCount(8);
        ModuloCouponShardRouter router = new ModuloCouponShardRouter(properties);

        ShardTarget first = router.routeCouponBatchTask("BATCH-20260603-001");
        ShardTarget second = router.routeCouponBatchTask("BATCH-20260603-001");

        assertThat(first).isEqualTo(second);
        assertThat(first.actualTable()).startsWith("coupon_batch_task_");
    }

    @Test
    void routeShouldRejectInvalidShardingKey() {
        ModuloCouponShardRouter router = new ModuloCouponShardRouter(new ShardingProperties());

        assertThatThrownBy(() -> router.routeUserCoupon(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("分片键必须为正数");
        assertThatThrownBy(() -> router.routeCouponBatchTask(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("分片键不能为空");
    }
}
