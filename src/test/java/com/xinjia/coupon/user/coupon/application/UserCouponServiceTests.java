package com.xinjia.coupon.user.coupon.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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
import com.xinjia.coupon.support.InMemoryCampaignStockCache;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.InMemoryUserCouponRepository;
import com.xinjia.coupon.user.coupon.infrastructure.InMemoryReceiveRequestRepository;
import com.xinjia.coupon.user.coupon.infrastructure.UserCouponRepository;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

class UserCouponServiceTests {

    private CouponTemplateService couponTemplateService;
    private CouponCampaignService couponCampaignService;
    private InMemoryCampaignStockCache campaignStockCache;
    private UserCouponService userCouponService;

    @BeforeEach
    void setUp() {
        couponTemplateService = new CouponTemplateService(new InMemoryCouponTemplateRepository());
        campaignStockCache = new InMemoryCampaignStockCache();
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
    }

    @Test
    void receiveShouldCreateUserCoupon() {
        CouponCampaign campaign = createRunningCampaign();

        UserCoupon userCoupon = userCouponService.receive(receiveRequest("req-1", 10L, campaign.getId()));

        assertThat(userCoupon.getId()).isNotNull();
        assertThat(userCoupon.getUserId()).isEqualTo(10L);
        assertThat(userCoupon.getCampaignId()).isEqualTo(campaign.getId());
        assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.RECEIVED);
        assertThat(campaignStockCache.getStock(campaign.getId())).isEqualTo(499);
    }

    @Test
    void receiveShouldRejectNotRunningCampaign() {
        CouponTemplate template = createTemplate();
        CouponCampaign campaign = couponCampaignService.create(validCampaignRequest(template.getId()));

        assertThatThrownBy(() -> userCouponService.receive(receiveRequest("req-2", 10L, campaign.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("活动未开始或不可领取");
    }

    @Test
    void listByUserIdShouldReturnUserCoupons() {
        CouponCampaign campaign = createRunningCampaign();
        userCouponService.receive(receiveRequest("req-3", 10L, campaign.getId()));

        assertThat(userCouponService.listByUserId(10L)).hasSize(1);
        assertThat(userCouponService.listByUserId(11L)).isEmpty();
    }

    @Test
    void receiveShouldRejectWhenUserExceedsCampaignLimit() {
        CouponCampaign campaign = createRunningCampaign();
        userCouponService.receive(receiveRequest("req-4", 10L, campaign.getId()));

        assertThatThrownBy(() -> userCouponService.receive(receiveRequest("req-5", 10L, campaign.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户已达到该活动领取上限");
    }

    @Test
    void receiveShouldRejectWhenCampaignStockIsEmpty() {
        CouponCampaign campaign = createRunningCampaign(1);
        userCouponService.receive(receiveRequest("req-6", 10L, campaign.getId()));

        assertThatThrownBy(() -> userCouponService.receive(receiveRequest("req-7", 11L, campaign.getId())))
                .isInstanceOf(BusinessException.class)
                .hasMessage("活动库存不足");
    }

    @Test
    void receiveShouldReturnSameCouponWhenRequestIdRepeated() {
        CouponCampaign campaign = createRunningCampaign();

        UserCoupon first = userCouponService.receive(receiveRequest("req-repeat", 10L, campaign.getId()));
        UserCoupon second = userCouponService.receive(receiveRequest("req-repeat", 10L, campaign.getId()));

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(campaignStockCache.getStock(campaign.getId())).isEqualTo(499);
    }

    @Test
    void receiveShouldRestoreStockWhenUserCouponSaveFailed() {
        CouponCampaign campaign = createRunningCampaign();
        UserCouponService failingService = new UserCouponService(
                new FailingUserCouponRepository(),
                couponCampaignService,
                couponTemplateService,
                campaignStockCache,
                new InMemoryReceiveRequestRepository()
        );

        assertThatThrownBy(() -> failingService.receive(receiveRequest("req-fail", 10L, campaign.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("保存用户券失败");

        assertThat(campaignStockCache.getStock(campaign.getId())).isEqualTo(500);
    }

    private ReceiveCouponRequest receiveRequest(String requestId, Long userId, Long campaignId) {
        return new ReceiveCouponRequest(requestId, userId, campaignId);
    }

    private CouponCampaign createRunningCampaign() {
        return createRunningCampaign(500);
    }

    private CouponCampaign createRunningCampaign(Integer stock) {
        CouponTemplate template = createTemplate();
        CouponCampaign campaign = couponCampaignService.create(validCampaignRequest(template.getId(), stock));
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
        return validCampaignRequest(templateId, 500);
    }

    private CreateCouponCampaignRequest validCampaignRequest(Long templateId, Integer stock) {
        return new CreateCouponCampaignRequest(
                templateId,
                1L,
                "六月新人发券活动",
                stock,
                1,
                OffsetDateTime.now().minusHours(1),
                OffsetDateTime.now().plusDays(10)
        );
    }

    private static class FailingUserCouponRepository implements UserCouponRepository {

        @Override
        public UserCoupon save(UserCoupon userCoupon) {
            throw new IllegalStateException("保存用户券失败");
        }

        @Override
        public Optional<UserCoupon> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public List<UserCoupon> findByUserId(Long userId) {
            return List.of();
        }

        @Override
        public long countByUserIdAndCampaignId(Long userId, Long campaignId) {
            return 0;
        }
    }
}
