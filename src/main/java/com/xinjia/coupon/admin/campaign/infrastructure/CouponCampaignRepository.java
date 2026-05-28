package com.xinjia.coupon.admin.campaign.infrastructure;

import java.util.List;
import java.util.Optional;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;

public interface CouponCampaignRepository {

    CouponCampaign save(CouponCampaign campaign);

    Optional<CouponCampaign> findById(Long id);

    List<CouponCampaign> findAll();
}
