package com.xinjia.coupon.user.coupon.infrastructure;

public interface CampaignStockCache {

    void initializeStock(Long campaignId, Integer stock);

    void preheatStock(Long campaignId, Integer stock);

    boolean tryDeductStock(Long campaignId);

    StockDeductResult tryDeductStock(Long campaignId, Long userId, Integer perUserLimit);

    void restoreStock(Long campaignId);

    default void restoreStock(Long campaignId, Long userId) {
        restoreStock(campaignId);
    }
}
