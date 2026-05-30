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
import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.settlement.web.CouponCancelRequest;
import com.xinjia.coupon.settlement.web.CouponConfirmRequest;
import com.xinjia.coupon.settlement.web.CouponLockRequest;
import com.xinjia.coupon.settlement.web.CouponOperationView;
import com.xinjia.coupon.settlement.web.OrderItemRequest;
import com.xinjia.coupon.settlement.web.SettlementCalculateRequest;
import com.xinjia.coupon.settlement.web.SettlementCalculateView;
import com.xinjia.coupon.support.InMemoryCampaignStockCache;
import com.xinjia.coupon.user.coupon.application.UserCouponService;
import com.xinjia.coupon.user.coupon.infrastructure.InMemoryReceiveRequestRepository;
import com.xinjia.coupon.user.coupon.infrastructure.InMemoryUserCouponRepository;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;

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
        assertThat(result.availableCoupons().get(0).calculatedDiscountAmount()).isEqualTo(500L);
        assertThat(result.bestDiscountAmount()).isEqualTo(500L);
        assertThat(result.payableAmount()).isEqualTo(4500L);
    }

    @Test
    void calculateShouldFilterByMerchantAndThreshold() {
        CouponTemplate template = createTemplate(1L, 3000L, 500L);
        CouponCampaign campaign = createRunningCampaign(template.getId());
        userCouponService.receive(new ReceiveCouponRequest("settle-req-2", 10L, campaign.getId()));

        assertThat(settlementService.calculate(calculateRequest(10L, 2L, 5000L)).availableCoupons()).isEmpty();
        assertThat(settlementService.calculate(calculateRequest(10L, 1L, 2000L)).availableCoupons()).isEmpty();
    }

    @Test
    void calculateShouldChooseBestDiscountCoupon() {
        CouponTemplate fullReductionTemplate = createTemplate(1L, 3000L, 500L);
        CouponTemplate discountTemplate = createDiscountTemplate(1L, 85);
        CouponCampaign fullReductionCampaign = createRunningCampaign(fullReductionTemplate.getId());
        CouponCampaign discountCampaign = createRunningCampaign(discountTemplate.getId());
        userCouponService.receive(new ReceiveCouponRequest("settle-req-3", 10L, fullReductionCampaign.getId()));
        userCouponService.receive(new ReceiveCouponRequest("settle-req-4", 10L, discountCampaign.getId()));

        SettlementCalculateView result = settlementService.calculate(calculateRequest(10L, 1L, 5000L));

        assertThat(result.availableCoupons()).hasSize(2);
        assertThat(result.bestCoupon()).isNotNull();
        assertThat(result.bestCoupon().templateId()).isEqualTo(discountTemplate.getId());
        assertThat(result.bestDiscountAmount()).isEqualTo(750L);
        assertThat(result.payableAmount()).isEqualTo(4250L);
    }

    @Test
    void lockShouldChangeUserCouponToLocked() {
        UserCoupon userCoupon = receiveCoupon("settle-req-5", 10L);

        CouponOperationView result = settlementService.lock(new CouponLockRequest(
                10L,
                userCoupon.getId(),
                "ORDER-LOCK-1"
        ));

        assertThat(result.status()).isEqualTo(UserCouponStatus.LOCKED);
        assertThat(result.orderNo()).isEqualTo("ORDER-LOCK-1");
        assertThat(result.lockedAt()).isNotNull();
    }

    @Test
    void confirmShouldMarkLockedCouponUsed() {
        UserCoupon userCoupon = receiveCoupon("settle-req-6", 10L);
        settlementService.lock(new CouponLockRequest(10L, userCoupon.getId(), "ORDER-CONFIRM-1"));

        CouponOperationView result = settlementService.confirm(new CouponConfirmRequest(
                10L,
                userCoupon.getId(),
                "ORDER-CONFIRM-1"
        ));

        assertThat(result.status()).isEqualTo(UserCouponStatus.USED);
        assertThat(result.usedAt()).isNotNull();
    }

    @Test
    void cancelShouldReleaseLockedCouponAndMakeItAvailableAgain() {
        UserCoupon userCoupon = receiveCoupon("settle-req-7", 10L);
        settlementService.lock(new CouponLockRequest(10L, userCoupon.getId(), "ORDER-CANCEL-1"));

        CouponOperationView result = settlementService.cancel(new CouponCancelRequest(
                10L,
                userCoupon.getId(),
                "ORDER-CANCEL-1"
        ));

        SettlementCalculateView calculateResult = settlementService.calculate(calculateRequest(10L, 1L, 5000L));

        assertThat(result.status()).isEqualTo(UserCouponStatus.RECEIVED);
        assertThat(result.orderNo()).isNull();
        assertThat(calculateResult.availableCoupons()).hasSize(1);
        assertThat(calculateResult.availableCoupons().get(0).userCouponId()).isEqualTo(userCoupon.getId());
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

    private CouponTemplate createDiscountTemplate(Long merchantId, Integer discountRate) {
        return couponTemplateService.create(new CreateCouponTemplateRequest(
                merchantId,
                "订单折扣券",
                CouponType.DISCOUNT,
                null,
                discountRate,
                0L,
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

    private UserCoupon receiveCoupon(String requestId, Long userId) {
        CouponTemplate template = createTemplate(1L, 3000L, 500L);
        CouponCampaign campaign = createRunningCampaign(template.getId());
        return userCouponService.receive(new ReceiveCouponRequest(requestId, userId, campaign.getId()));
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
