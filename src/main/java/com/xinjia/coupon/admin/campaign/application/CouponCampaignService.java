package com.xinjia.coupon.admin.campaign.application;

import org.springframework.stereotype.Service;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.campaign.infrastructure.CouponCampaignRepository;
import com.xinjia.coupon.admin.campaign.web.CreateCouponCampaignRequest;
import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;

@Service
public class CouponCampaignService {

    private final CouponCampaignRepository couponCampaignRepository;
    private final CouponTemplateService couponTemplateService;

    public CouponCampaignService(
            CouponCampaignRepository couponCampaignRepository,
            CouponTemplateService couponTemplateService
    ) {
        this.couponCampaignRepository = couponCampaignRepository;
        this.couponTemplateService = couponTemplateService;
    }

    public CouponCampaign create(CreateCouponCampaignRequest request) {
        couponTemplateService.getById(request.templateId());
        validateTimeRange(request);

        CouponCampaign campaign = CouponCampaign.create(
                request.templateId(),
                request.merchantId(),
                request.name(),
                request.campaignStock(),
                request.perUserLimit(),
                request.startTime(),
                request.endTime()
        );
        return couponCampaignRepository.save(campaign);
    }

    private void validateTimeRange(CreateCouponCampaignRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "活动结束时间必须晚于开始时间");
        }
    }
}
