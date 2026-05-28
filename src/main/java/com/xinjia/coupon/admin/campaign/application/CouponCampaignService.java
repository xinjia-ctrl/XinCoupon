package com.xinjia.coupon.admin.campaign.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.campaign.infrastructure.CouponCampaignRepository;
import com.xinjia.coupon.admin.campaign.web.CreateCouponCampaignRequest;
import com.xinjia.coupon.admin.campaign.web.UpdateCouponCampaignStatusRequest;
import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.common.enums.CampaignStatus;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;

@Service
public class CouponCampaignService {

    private static final Set<CampaignStatus> TERMINAL_STATUSES = Set.of(
            CampaignStatus.FINISHED,
            CampaignStatus.CANCELED
    );

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

    public CouponCampaign getById(Long campaignId) {
        return couponCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "发券活动不存在"));
    }

    public List<CouponCampaign> list() {
        return couponCampaignRepository.findAll();
    }

    public CouponCampaign changeStatus(Long campaignId, UpdateCouponCampaignStatusRequest request) {
        CouponCampaign campaign = getById(campaignId);
        validateStatusChange(campaign, request.status());
        campaign.changeStatus(request.status());
        return couponCampaignRepository.save(campaign);
    }

    public void ensureReceivable(Long campaignId) {
        CouponCampaign campaign = getById(campaignId);
        if (campaign.getStatus() != CampaignStatus.RUNNING) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "活动未开始或不可领取");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(campaign.getStartTime()) || now.isAfter(campaign.getEndTime())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "当前不在活动领取时间内");
        }
    }

    private void validateTimeRange(CreateCouponCampaignRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "活动结束时间必须晚于开始时间");
        }
    }

    private void validateStatusChange(CouponCampaign campaign, CampaignStatus targetStatus) {
        if (TERMINAL_STATUSES.contains(campaign.getStatus()) && campaign.getStatus() != targetStatus) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "已结束或已取消的活动不能再次变更状态");
        }
    }
}
