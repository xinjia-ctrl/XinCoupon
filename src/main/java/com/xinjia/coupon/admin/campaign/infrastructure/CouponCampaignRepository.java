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

    default boolean tryDeductStock(Long id, int count) {
        if (count <= 0) {
            return true;
        }
        for (int i = 0; i < count; i++) {
            if (!tryDeductStock(id)) {
                for (int j = 0; j < i; j++) {
                    restoreStock(id);
                }
                return false;
            }
        }
        return true;
    }

    void restoreStock(Long id);

    default void restoreStock(Long id, int count) {
        for (int i = 0; i < count; i++) {
            restoreStock(id);
        }
    }
}
