package com.xinjia.coupon.admin.campaign.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.admin.campaign.application.CouponCampaignService;
import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.common.api.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/coupon-campaigns")
public class CouponCampaignController {

    private final CouponCampaignService couponCampaignService;

    public CouponCampaignController(CouponCampaignService couponCampaignService) {
        this.couponCampaignService = couponCampaignService;
    }

    @PostMapping
    public ApiResponse<CouponCampaignView> create(@Valid @RequestBody CreateCouponCampaignRequest request) {
        CouponCampaign campaign = couponCampaignService.create(request);
        return ApiResponse.success(CouponCampaignView.from(campaign));
    }
}
