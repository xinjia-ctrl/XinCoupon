package com.xinjia.coupon.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.xinjia.coupon.user.coupon.infrastructure.CampaignStockCache;

public class InMemoryCampaignStockCache implements CampaignStockCache {

    private final ConcurrentMap<Long, Integer> stocks = new ConcurrentHashMap<>();

    @Override
    public void initializeStock(Long campaignId, Integer stock) {
        stocks.putIfAbsent(campaignId, stock);
    }

    public Integer getStock(Long campaignId) {
        return stocks.get(campaignId);
    }
}
