package com.xinjia.coupon.admin.campaign.infrastructure;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;

@Repository
public class InMemoryCouponCampaignRepository implements CouponCampaignRepository {

    private final AtomicLong idGenerator = new AtomicLong(2000);
    private final ConcurrentMap<Long, CouponCampaign> campaigns = new ConcurrentHashMap<>();

    @Override
    public CouponCampaign save(CouponCampaign campaign) {
        if (campaign.getId() == null) {
            campaign.assignId(idGenerator.incrementAndGet());
        }
        campaigns.put(campaign.getId(), campaign);
        return campaign;
    }
}
