package com.xinjia.coupon.user.coupon.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xinjia.coupon.admin.campaign.application.CouponCampaignService;
import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.CampaignStockCache;
import com.xinjia.coupon.user.coupon.infrastructure.UserCouponRepository;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

@Service
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponCampaignService couponCampaignService;
    private final CouponTemplateService couponTemplateService;
    private final CampaignStockCache campaignStockCache;

    public UserCouponService(
            UserCouponRepository userCouponRepository,
            CouponCampaignService couponCampaignService,
            CouponTemplateService couponTemplateService,
            CampaignStockCache campaignStockCache
    ) {
        this.userCouponRepository = userCouponRepository;
        this.couponCampaignService = couponCampaignService;
        this.couponTemplateService = couponTemplateService;
        this.campaignStockCache = campaignStockCache;
    }

    public UserCoupon receive(ReceiveCouponRequest request) {
        couponCampaignService.ensureReceivable(request.campaignId());
        CouponCampaign campaign = couponCampaignService.getById(request.campaignId());
        validateReceiveLimit(request.userId(), campaign);
        deductStock(campaign.getId());
        CouponTemplate template = couponTemplateService.getById(campaign.getTemplateId());

        UserCoupon userCoupon = UserCoupon.receive(
                request.userId(),
                template.getId(),
                campaign.getId(),
                template.getValidEndTime()
        );
        return userCouponRepository.save(userCoupon);
    }

    public List<UserCoupon> listByUserId(Long userId) {
        return userCouponRepository.findByUserId(userId);
    }

    private void deductStock(Long campaignId) {
        if (!campaignStockCache.tryDeductStock(campaignId)) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "活动库存不足");
        }
    }

    private void validateReceiveLimit(Long userId, CouponCampaign campaign) {
        long receivedCount = userCouponRepository.countByUserIdAndCampaignId(userId, campaign.getId());
        if (receivedCount >= campaign.getPerUserLimit()) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "用户已达到该活动领取上限");
        }
    }
}
