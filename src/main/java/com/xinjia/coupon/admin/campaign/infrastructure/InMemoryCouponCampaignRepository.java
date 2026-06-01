package com.xinjia.coupon.admin.campaign.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.common.enums.CampaignStatus;

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

    @Override
    public Optional<CouponCampaign> findById(Long id) {
        return Optional.ofNullable(campaigns.get(id));
    }

    @Override
    public List<CouponCampaign> findAll() {
        return campaigns.values()
                .stream()
                .sorted(Comparator.comparing(CouponCampaign::getId).reversed())
                .toList();
    }

    @Override
    public Optional<CouponCampaign> updateStatus(Long id, CampaignStatus status) {
        return findById(id)
                .map(campaign -> {
                    campaign.changeStatus(status);
                    return campaign;
                });
    }

    @Override
    public boolean tryDeductStock(Long id) {
        return findById(id)
                .map(CouponCampaign::deductStock)
                .orElse(false);
    }

    @Override
    public void restoreStock(Long id) {
        findById(id).ifPresent(CouponCampaign::restoreStock);
    }
}
