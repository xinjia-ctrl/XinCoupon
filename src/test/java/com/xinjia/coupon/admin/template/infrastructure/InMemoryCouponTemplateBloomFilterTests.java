package com.xinjia.coupon.admin.template.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InMemoryCouponTemplateBloomFilterTests {

    @Test
    void bloomFilterShouldRejectDefinitelyMissingTemplateId() {
        InMemoryCouponTemplateBloomFilter bloomFilter = new InMemoryCouponTemplateBloomFilter();
        bloomFilter.put(1001L);

        assertThat(bloomFilter.mightContain(1001L)).isTrue();
        assertThat(bloomFilter.mightContain(2002L)).isFalse();
    }
}
