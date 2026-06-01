package com.xinjia.coupon.user.coupon.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xinjia.coupon.admin.campaign.application.CouponCampaignService;
import com.xinjia.coupon.admin.campaign.domain.CouponCampaign;
import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.dispatch.event.application.CouponEventPublisher;
import com.xinjia.coupon.dispatch.event.domain.CouponReceivedEvent;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.CampaignStockCache;
import com.xinjia.coupon.user.coupon.infrastructure.ReceiveRequestRepository;
import com.xinjia.coupon.user.coupon.infrastructure.UserCouponRepository;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

@Service
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponCampaignService couponCampaignService;
    private final CouponTemplateService couponTemplateService;
    private final CampaignStockCache campaignStockCache;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final CouponEventPublisher couponEventPublisher;

    public UserCouponService(
            UserCouponRepository userCouponRepository,
            CouponCampaignService couponCampaignService,
            CouponTemplateService couponTemplateService,
            CampaignStockCache campaignStockCache,
            ReceiveRequestRepository receiveRequestRepository,
            CouponEventPublisher couponEventPublisher
    ) {
        this.userCouponRepository = userCouponRepository;
        this.couponCampaignService = couponCampaignService;
        this.couponTemplateService = couponTemplateService;
        this.campaignStockCache = campaignStockCache;
        this.receiveRequestRepository = receiveRequestRepository;
        this.couponEventPublisher = couponEventPublisher;
    }

    @Transactional
    public UserCoupon receive(ReceiveCouponRequest request) {
        return receiveRequestRepository.findResult(request.requestId())
                .orElseGet(() -> doReceive(request));
    }

    private UserCoupon doReceive(ReceiveCouponRequest request) {
        couponCampaignService.ensureReceivable(request.campaignId());
        CouponCampaign campaign = couponCampaignService.getById(request.campaignId());
        validateReceiveLimit(request.userId(), campaign);
        deductStock(campaign.getId());
        try {
            couponCampaignService.deductDatabaseStock(campaign.getId());
            return createUserCoupon(request, campaign);
        } catch (RuntimeException exception) {
            campaignStockCache.restoreStock(campaign.getId());
            couponCampaignService.restoreDatabaseStock(campaign.getId());
            throw exception;
        }
    }

    private UserCoupon createUserCoupon(ReceiveCouponRequest request, CouponCampaign campaign) {
        CouponTemplate template = couponTemplateService.getById(campaign.getTemplateId());

        UserCoupon userCoupon = UserCoupon.receive(
                request.userId(),
                template.getId(),
                campaign.getId(),
                template.getValidEndTime()
        );
        UserCoupon saved = userCouponRepository.save(userCoupon);
        receiveRequestRepository.saveResult(request.requestId(), saved);
        publishReceivedEvent(saved);
        return saved;
    }

    private void publishReceivedEvent(UserCoupon userCoupon) {
        couponEventPublisher.publish(new CouponReceivedEvent(
                UUID.randomUUID().toString(),
                userCoupon.getUserId(),
                userCoupon.getId(),
                userCoupon.getTemplateId(),
                userCoupon.getCampaignId(),
                OffsetDateTime.now()
        ));
    }

    @Transactional(readOnly = true)
    public List<UserCoupon> listByUserId(Long userId) {
        return userCouponRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<UserCoupon> listByUserIdAndStatus(Long userId, UserCouponStatus status) {
        return userCouponRepository.findByUserIdAndStatus(userId, status);
    }

    @Transactional
    public UserCoupon lock(Long userId, Long userCouponId, String orderNo) {
        UserCoupon userCoupon = getOwnedCoupon(userId, userCouponId);
        if (userCoupon.getStatus() != UserCouponStatus.RECEIVED) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券当前状态不可锁定");
        }
        if (!userCoupon.getExpiredAt().isAfter(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券已过期");
        }
        userCoupon.lock(orderNo);
        return userCouponRepository.save(userCoupon);
    }

    @Transactional
    public UserCoupon confirm(Long userId, Long userCouponId, String orderNo) {
        UserCoupon userCoupon = getLockedCoupon(userId, userCouponId, orderNo);
        userCoupon.confirmUse();
        return userCouponRepository.save(userCoupon);
    }

    @Transactional
    public UserCoupon cancel(Long userId, Long userCouponId, String orderNo) {
        UserCoupon userCoupon = getLockedCoupon(userId, userCouponId, orderNo);
        userCoupon.release();
        return userCouponRepository.save(userCoupon);
    }

    private UserCoupon getOwnedCoupon(Long userId, Long userCouponId) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户优惠券不存在"));
        if (!userCoupon.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "用户优惠券归属不匹配");
        }
        return userCoupon;
    }

    private UserCoupon getLockedCoupon(Long userId, Long userCouponId, String orderNo) {
        UserCoupon userCoupon = getOwnedCoupon(userId, userCouponId);
        if (userCoupon.getStatus() != UserCouponStatus.LOCKED) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券当前状态不是锁定状态");
        }
        if (!orderNo.equals(userCoupon.getOrderNo())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "订单号与锁券记录不匹配");
        }
        return userCoupon;
    }

    private void deductStock(Long campaignId) {
        if (!campaignStockCache.tryDeductStock(campaignId)) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "活动库存不足");
        }
    }

    private void validateReceiveLimit(Long userId, CouponCampaign campaign) {
        long receivedCount = userCouponRepository.countByUserIdAndCampaignId(userId, campaign.getId());
        if (receivedCount >= campaign.getPerUserLimit()) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "用户已达到该活动领取上限");
        }
    }
}
