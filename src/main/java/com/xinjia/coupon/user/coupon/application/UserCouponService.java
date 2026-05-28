package com.xinjia.coupon.user.coupon.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xinjia.coupon.admin.campaign.application.CouponCampaignService;
import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.UserCouponRepository;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

@Service
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponCampaignService couponCampaignService;
    private final CouponTemplateService couponTemplateService;

    public UserCouponService(
            UserCouponRepository userCouponRepository,
            CouponCampaignService couponCampaignService,
            CouponTemplateService couponTemplateService
    ) {
        this.userCouponRepository = userCouponRepository;
        this.couponCampaignService = couponCampaignService;
        this.couponTemplateService = couponTemplateService;
    }

    public UserCoupon receive(ReceiveCouponRequest request) {
        couponCampaignService.ensureReceivable(request.campaignId());
        CouponCampaign campaign = couponCampaignService.getById(request.campaignId());
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
}
