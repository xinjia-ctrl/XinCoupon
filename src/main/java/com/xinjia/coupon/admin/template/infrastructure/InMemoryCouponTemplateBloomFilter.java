package com.xinjia.coupon.admin.template.infrastructure;

import java.util.BitSet;

import org.springframework.stereotype.Component;

@Component
public class InMemoryCouponTemplateBloomFilter implements CouponTemplateBloomFilter {

    private static final int BIT_SIZE = 1 << 20;
    private static final int[] SEEDS = {31, 131, 1313};

    private final BitSet bitSet = new BitSet(BIT_SIZE);

    @Override
    public synchronized void put(Long templateId) {
        for (int seed : SEEDS) {
            bitSet.set(hash(templateId, seed));
        }
    }

    @Override
    public synchronized boolean mightContain(Long templateId) {
        for (int seed : SEEDS) {
            if (!bitSet.get(hash(templateId, seed))) {
                return false;
            }
        }
        return true;
    }

    private int hash(Long value, int seed) {
        long hashed = value == null ? 0L : value * seed;
        return (int) (Math.abs(hashed) % BIT_SIZE);
    }
}
