package com.xinjia.coupon.admin.campaign.infrastructure;

import java.util.List;
import java.util.Optional;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.common.enums.CampaignStatus;

public interface CouponCampaignRepository {

    CouponCampaign save(CouponCampaign campaign);

    Optional<CouponCampaign> findById(Long id);

    List<CouponCampaign> findAll();

    Optional<CouponCampaign> updateStatus(Long id, CampaignStatus status);

    boolean tryDeductStock(Long id);

    void restoreStock(Long id);
}
