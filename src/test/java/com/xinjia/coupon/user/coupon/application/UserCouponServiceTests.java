package com.xinjia.coupon.user.coupon.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.xinjia.coupon.admin.campaign.application.CouponCampaignService;
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
import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.InMemoryUserCouponRepository;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

class UserCouponServiceTests {

    private CouponTemplateService couponTemplateService;
    private CouponCampaignService couponCampaignService;
    private UserCouponService userCouponService;

    @BeforeEach
    void setUp() {
        couponTemplateService = new CouponTemplateService(new InMemoryCouponTemplateRepository());
        couponCampaignService = new CouponCampaignService(new InMemoryCouponCampaignRepository(), couponTemplateService);
        userCouponService = new UserCouponService(
                new InMemoryUserCouponRepository(),
                couponCampaignService,
                couponTemplateService
        );
    }

    @Test
    void receiveShouldCreateUserCoupon() {
        CouponCampaign campaign = createRunningCampaign();

        UserCoupon userCoupon = userCouponService.receive(new ReceiveCouponRequest(10L, campaign.getId()));

        assertThat(userCoupon.getId()).isNotNull();
        assertThat(userCoupon.getUserId()).isEqualTo(10L);
        assertThat(userCoupon.getCampaignId()).isEqualTo(campaign.getId());
        assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.RECEIVED);
    }

    @Test
    void receiveShouldRejectNotRunningCampaign() {
        CouponTemplate template = createTemplate();
        CouponCampaign campaign = couponCampaignService.create(validCampaignRequest(template.getId()));

        assertThatThrownBy(() -> userCouponService.receive(new ReceiveCouponRequest(10L, campaign.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("活动未开始或不可领取");
    }

    @Test
    void listByUserIdShouldReturnUserCoupons() {
        CouponCampaign campaign = createRunningCampaign();
        userCouponService.receive(new ReceiveCouponRequest(10L, campaign.getId()));

        assertThat(userCouponService.listByUserId(10L)).hasSize(1);
        assertThat(userCouponService.listByUserId(11L)).isEmpty();
    }

    @Test
    void receiveShouldRejectWhenUserExceedsCampaignLimit() {
        CouponCampaign campaign = createRunningCampaign();
        userCouponService.receive(new ReceiveCouponRequest(10L, campaign.getId()));

        assertThatThrownBy(() -> userCouponService.receive(new ReceiveCouponRequest(10L, campaign.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户已达到该活动领取上限");
    }

    private CouponCampaign createRunningCampaign() {
        CouponTemplate template = createTemplate();
        CouponCampaign campaign = couponCampaignService.create(validCampaignRequest(template.getId()));
        return couponCampaignService.changeStatus(
                campaign.getId(),
                new UpdateCouponCampaignStatusRequest(CampaignStatus.RUNNING)
        );
    }

    private CouponTemplate createTemplate() {
        return couponTemplateService.create(new CreateCouponTemplateRequest(
                1L,
                "新人满减券",
                CouponType.FULL_REDUCTION,
                500L,
                null,
                3000L,
                OffsetDateTime.now().minusDays(1),
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
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now().plusDays(10)
        );
    }
}
