package com.xinjia.coupon.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.xinjia.coupon.user.coupon.infrastructure.CampaignStockCache;

public class InMemoryCampaignStockCache implements CampaignStockCache {

    private final ConcurrentMap<Long, AtomicInteger> stocks = new ConcurrentHashMap<>();

    @Override
    public void initializeStock(Long campaignId, Integer stock) {
        stocks.putIfAbsent(campaignId, new AtomicInteger(stock));
    }

    @Override
    public boolean tryDeductStock(Long campaignId) {
        AtomicInteger stock = stocks.get(campaignId);
        if (stock == null) {
            return false;
        }
        while (true) {
            int current = stock.get();
            if (current <= 0) {
                return false;
            }
            if (stock.compareAndSet(current, current - 1)) {
                return true;
            }
        }
    }

    public Integer getStock(Long campaignId) {
        AtomicInteger stock = stocks.get(campaignId);
        return stock == null ? null : stock.get();
    }
}
