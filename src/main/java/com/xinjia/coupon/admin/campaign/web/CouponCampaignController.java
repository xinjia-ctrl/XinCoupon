package com.xinjia.coupon.admin.campaign.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{campaignId}")
    public ApiResponse<CouponCampaignView> getById(@PathVariable Long campaignId) {
        CouponCampaign campaign = couponCampaignService.getById(campaignId);
        return ApiResponse.success(CouponCampaignView.from(campaign));
    }

    @GetMapping
    public ApiResponse<List<CouponCampaignView>> list() {
        List<CouponCampaignView> campaigns = couponCampaignService.list()
                .stream()
                .map(CouponCampaignView::from)
                .toList();
        return ApiResponse.success(campaigns);
    }

    @PatchMapping("/{campaignId}/status")
    public ApiResponse<CouponCampaignView> changeStatus(
            @PathVariable Long campaignId,
            @Valid @RequestBody UpdateCouponCampaignStatusRequest request
    ) {
        CouponCampaign campaign = couponCampaignService.changeStatus(campaignId, request);
        return ApiResponse.success(CouponCampaignView.from(campaign));
    }
}
