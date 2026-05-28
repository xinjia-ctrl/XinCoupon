package com.xinjia.coupon.admin.campaign.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.campaign.infrastructure.InMemoryCouponCampaignRepository;
import com.xinjia.coupon.admin.campaign.web.CreateCouponCampaignRequest;
import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.admin.template.infrastructure.InMemoryCouponTemplateRepository;
import com.xinjia.coupon.admin.template.web.CreateCouponTemplateRequest;
import com.xinjia.coupon.common.enums.CampaignStatus;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.common.exception.BusinessException;

class CouponCampaignServiceTests {

    private CouponTemplateService couponTemplateService;
    private CouponCampaignService couponCampaignService;

    @BeforeEach
    void setUp() {
        couponTemplateService = new CouponTemplateService(new InMemoryCouponTemplateRepository());
        couponCampaignService = new CouponCampaignService(
                new InMemoryCouponCampaignRepository(),
                couponTemplateService
        );
    }

    @Test
    void createShouldSavePendingCampaign() {
        CouponTemplate template = createTemplate();

        CouponCampaign campaign = couponCampaignService.create(validCampaignRequest(template.getId()));

        assertThat(campaign.getId()).isNotNull();
        assertThat(campaign.getTemplateId()).isEqualTo(template.getId());
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.PENDING);
        assertThat(campaign.getReceivedCount()).isZero();
    }

    @Test
    void createShouldRejectMissingTemplate() {
        assertThatThrownBy(() -> couponCampaignService.create(validCampaignRequest(404L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("优惠券模板不存在");
    }

    @Test
    void createShouldRejectInvalidTimeRange() {
        CouponTemplate template = createTemplate();
        CreateCouponCampaignRequest request = new CreateCouponCampaignRequest(
                template.getId(),
                1L,
                "时间错误活动",
                100,
                1,
                OffsetDateTime.now().plusDays(5),
                OffsetDateTime.now().plusDays(2)
        );

        assertThatThrownBy(() -> couponCampaignService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("活动结束时间必须晚于开始时间");
    }

    private CouponTemplate createTemplate() {
        return couponTemplateService.create(new CreateCouponTemplateRequest(
                1L,
                "新人满减券",
                CouponType.FULL_REDUCTION,
                500L,
                null,
                3000L,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(30),
                1000
        ));
    }

    private CreateCouponCampaignRequest validCampaignRequest(Long templateId) {
        return new CreateCouponCampaignRequest(
                templateId,
                1L,
                "六月新人发券活动",
                500,
                1,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(10)
        );
    }
}
