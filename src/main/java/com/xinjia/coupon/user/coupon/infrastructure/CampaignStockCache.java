package com.xinjia.coupon.user.coupon.infrastructure;

public interface CampaignStockCache {

    void initializeStock(Long campaignId, Integer stock);

    boolean tryDeductStock(Long campaignId);

    void restoreStock(Long campaignId);
}
