package com.xinjia.coupon.settlement.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

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
import com.xinjia.coupon.settlement.web.OrderItemRequest;
import com.xinjia.coupon.settlement.web.SettlementCalculateRequest;
import com.xinjia.coupon.settlement.web.SettlementCalculateView;
import com.xinjia.coupon.support.InMemoryCampaignStockCache;
import com.xinjia.coupon.user.coupon.application.UserCouponService;
import com.xinjia.coupon.user.coupon.infrastructure.InMemoryReceiveRequestRepository;
import com.xinjia.coupon.user.coupon.infrastructure.InMemoryUserCouponRepository;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

class SettlementServiceTests {

    private CouponTemplateService couponTemplateService;
    private CouponCampaignService couponCampaignService;
    private UserCouponService userCouponService;
    private SettlementService settlementService;

    @BeforeEach
    void setUp() {
        couponTemplateService = new CouponTemplateService(new InMemoryCouponTemplateRepository());
        InMemoryCampaignStockCache campaignStockCache = new InMemoryCampaignStockCache();
        couponCampaignService = new CouponCampaignService(
                new InMemoryCouponCampaignRepository(),
                couponTemplateService,
                campaignStockCache
        );
        userCouponService = new UserCouponService(
                new InMemoryUserCouponRepository(),
                couponCampaignService,
                couponTemplateService,
                campaignStockCache,
                new InMemoryReceiveRequestRepository()
        );
        settlementService = new SettlementService(userCouponService, couponTemplateService);
    }

    @Test
    void calculateShouldReturnAvailableCoupons() {
        CouponTemplate template = createTemplate(1L, 3000L, 500L);
        CouponCampaign campaign = createRunningCampaign(template.getId());
        userCouponService.receive(new ReceiveCouponRequest("settle-req-1", 10L, campaign.getId()));

        SettlementCalculateView result = settlementService.calculate(calculateRequest(10L, 1L, 5000L));

        assertThat(result.availableCoupons()).hasSize(1);
        assertThat(result.availableCoupons().get(0).templateId()).isEqualTo(template.getId());
        assertThat(result.payableAmount()).isEqualTo(5000L);
    }

    @Test
    void calculateShouldFilterByMerchantAndThreshold() {
        CouponTemplate template = createTemplate(1L, 3000L, 500L);
        CouponCampaign campaign = createRunningCampaign(template.getId());
        userCouponService.receive(new ReceiveCouponRequest("settle-req-2", 10L, campaign.getId()));

        assertThat(settlementService.calculate(calculateRequest(10L, 2L, 5000L)).availableCoupons()).isEmpty();
        assertThat(settlementService.calculate(calculateRequest(10L, 1L, 2000L)).availableCoupons()).isEmpty();
    }

    private CouponTemplate createTemplate(Long merchantId, Long thresholdAmount, Long discountAmount) {
        return couponTemplateService.create(new CreateCouponTemplateRequest(
                merchantId,
                "订单满减券",
                CouponType.FULL_REDUCTION,
                discountAmount,
                null,
                thresholdAmount,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusDays(30),
                1000
        ));
    }

    private CouponCampaign createRunningCampaign(Long templateId) {
        CouponCampaign campaign = couponCampaignService.create(new CreateCouponCampaignRequest(
                templateId,
                1L,
                "订单结算测试活动",
                100,
                3,
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now().plusDays(10)
        ));
        return couponCampaignService.changeStatus(
                campaign.getId(),
                new UpdateCouponCampaignStatusRequest(CampaignStatus.RUNNING)
        );
    }

    private SettlementCalculateRequest calculateRequest(Long userId, Long merchantId, Long orderAmount) {
        return new SettlementCalculateRequest(
                userId,
                "ORDER-" + userId + "-" + merchantId + "-" + orderAmount,
                merchantId,
                orderAmount,
                List.of(new OrderItemRequest("SKU-1", "FOOD", 1, orderAmount))
        );
    }
}
