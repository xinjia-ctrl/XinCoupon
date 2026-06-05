package com.xinjia.coupon.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.xinjia.coupon.user.coupon.infrastructure.CampaignStockCache;
import com.xinjia.coupon.user.coupon.infrastructure.StockDeductResult;
import com.xinjia.coupon.user.coupon.infrastructure.StockDeductStatus;

public class InMemoryCampaignStockCache implements CampaignStockCache {

    private final ConcurrentMap<Long, AtomicInteger> stocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicInteger> receiveCounts = new ConcurrentHashMap<>();

    @Override
    public void initializeStock(Long campaignId, Integer stock) {
        stocks.putIfAbsent(campaignId, new AtomicInteger(stock));
    }

    @Override
    public void preheatStock(Long campaignId, Integer stock) {
        stocks.put(campaignId, new AtomicInteger(stock));
    }

    @Override
    public boolean tryDeductStock(Long campaignId) {
        return tryDeductStock(campaignId, 0L, Integer.MAX_VALUE).success();
    }

    @Override
    public StockDeductResult tryDeductStock(Long campaignId, Long userId, Integer perUserLimit) {
        AtomicInteger stock = stocks.get(campaignId);
        if (stock == null) {
            return new StockDeductResult(StockDeductStatus.STOCK_NOT_FOUND, 0);
        }
        String receiveCountKey = buildReceiveCountKey(campaignId, userId);
        AtomicInteger receiveCount = receiveCounts.computeIfAbsent(receiveCountKey, ignored -> new AtomicInteger());
        while (true) {
            int currentReceiveCount = receiveCount.get();
            if (currentReceiveCount >= perUserLimit) {
                return new StockDeductResult(StockDeductStatus.RECEIVE_LIMIT_EXCEEDED, currentReceiveCount);
            }
            int current = stock.get();
            if (current <= 0) {
                return new StockDeductResult(StockDeductStatus.STOCK_EMPTY, currentReceiveCount);
            }
            if (receiveCount.compareAndSet(currentReceiveCount, currentReceiveCount + 1)) {
                if (stock.compareAndSet(current, current - 1)) {
                    return new StockDeductResult(StockDeductStatus.SUCCESS, currentReceiveCount + 1L);
                }
                receiveCount.decrementAndGet();
            }
        }
    }

    @Override
    public void restoreStock(Long campaignId) {
        stocks.computeIfAbsent(campaignId, ignored -> new AtomicInteger()).incrementAndGet();
    }

    @Override
    public void restoreStock(Long campaignId, Long userId) {
        restoreStock(campaignId);
        receiveCounts.computeIfAbsent(buildReceiveCountKey(campaignId, userId), ignored -> new AtomicInteger())
                .updateAndGet(value -> Math.max(0, value - 1));
    }

    public Integer getStock(Long campaignId) {
        AtomicInteger stock = stocks.get(campaignId);
        return stock == null ? null : stock.get();
    }

    public Integer getReceiveCount(Long campaignId, Long userId) {
        AtomicInteger receiveCount = receiveCounts.get(buildReceiveCountKey(campaignId, userId));
        return receiveCount == null ? null : receiveCount.get();
    }

    private String buildReceiveCountKey(Long campaignId, Long userId) {
        return campaignId + ":" + userId;
    }
}
