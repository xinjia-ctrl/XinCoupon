package com.xinjia.coupon.admin.campaign.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.campaign.infrastructure.InMemoryCouponCampaignRepository;
import com.xinjia.coupon.admin.campaign.web.CreateCouponCampaignRequest;
import com.xinjia.coupon.admin.campaign.web.UpdateCouponCampaignStatusRequest;
import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.admin.template.infrastructure.InMemoryCouponTemplateRepository;
import com.xinjia.coupon.admin.template.web.CreateCouponTemplateRequest;
import com.xinjia.coupon.common.enums.CampaignStatus;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.support.InMemoryCampaignStockCache;

class CouponCampaignServiceTests {

    private CouponTemplateService couponTemplateService;
    private CouponCampaignService couponCampaignService;

    @BeforeEach
    void setUp() {
        couponTemplateService = new CouponTemplateService(new InMemoryCouponTemplateRepository());
        couponCampaignService = new CouponCampaignService(
                new InMemoryCouponCampaignRepository(),
                couponTemplateService,
                new InMemoryCampaignStockCache()
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

    @Test
    void getByIdShouldReturnCampaign() {
        CouponTemplate template = createTemplate();
        CouponCampaign saved = couponCampaignService.create(validCampaignRequest(template.getId()));

        CouponCampaign campaign = couponCampaignService.getById(saved.getId());

        assertThat(campaign.getId()).isEqualTo(saved.getId());
        assertThat(campaign.getName()).isEqualTo("六月新人发券活动");
    }

    @Test
    void listShouldReturnCreatedCampaigns() {
        CouponTemplate template = createTemplate();
        couponCampaignService.create(validCampaignRequest(template.getId()));

        assertThat(couponCampaignService.list()).hasSize(1);
    }

    @Test
    void changeStatusShouldUpdateCampaignStatus() {
        CouponTemplate template = createTemplate();
        CouponCampaign saved = couponCampaignService.create(validCampaignRequest(template.getId()));

        CouponCampaign campaign = couponCampaignService.changeStatus(
                saved.getId(),
                new UpdateCouponCampaignStatusRequest(CampaignStatus.RUNNING)
        );

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.RUNNING);
    }

    @Test
    void changeStatusShouldRejectTerminalCampaign() {
        CouponTemplate template = createTemplate();
        CouponCampaign saved = couponCampaignService.create(validCampaignRequest(template.getId()));

        couponCampaignService.changeStatus(saved.getId(), new UpdateCouponCampaignStatusRequest(CampaignStatus.CANCELED));

        assertThatThrownBy(() -> couponCampaignService.changeStatus(
                saved.getId(),
                new UpdateCouponCampaignStatusRequest(CampaignStatus.RUNNING)
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已结束或已取消的活动不能再次变更状态");
    }

    @Test
    void getByIdShouldRejectMissingCampaign() {
        assertThatThrownBy(() -> couponCampaignService.getById(404L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("发券活动不存在");
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
