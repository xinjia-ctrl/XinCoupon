package com.xinjia.coupon.common.sharding;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShardingPropertiesTests {

    @Test
    void shouldDistinguishManualAndShardingSphereModes() {
        ShardingProperties properties = new ShardingProperties();
        properties.setEnabled(true);

        assertThat(properties.isManualEnabled()).isTrue();
        assertThat(properties.isShardingSphereEnabled()).isFalse();

        properties.setMode(ShardingProperties.ShardingMode.SHARDINGSPHERE);

        assertThat(properties.isManualEnabled()).isFalse();
        assertThat(properties.isShardingSphereEnabled()).isTrue();
    }
}
