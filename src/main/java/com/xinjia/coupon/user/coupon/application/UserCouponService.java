package com.xinjia.coupon.user.coupon.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.xinjia.coupon.user.coupon.domain.CouponReceiveRequestedEvent;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;
import com.xinjia.coupon.user.coupon.infrastructure.CampaignStockCache;
import com.xinjia.coupon.user.coupon.infrastructure.ReceiveRequestRepository;
import com.xinjia.coupon.user.coupon.infrastructure.StockDeductResult;
import com.xinjia.coupon.user.coupon.infrastructure.StockDeductStatus;
import com.xinjia.coupon.user.coupon.infrastructure.UserCouponRepository;
import com.xinjia.coupon.user.coupon.web.CouponReceiveAcceptedView;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

@Service
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponCampaignService couponCampaignService;
    private final CouponTemplateService couponTemplateService;
    private final CampaignStockCache campaignStockCache;
    private final ReceiveRequestRepository receiveRequestRepository;
    private final CouponEventPublisher couponEventPublisher;
    private final CouponReceiveRequestPublisher couponReceiveRequestPublisher;

    public UserCouponService(
            UserCouponRepository userCouponRepository,
            CouponCampaignService couponCampaignService,
            CouponTemplateService couponTemplateService,
            CampaignStockCache campaignStockCache,
            ReceiveRequestRepository receiveRequestRepository,
            CouponEventPublisher couponEventPublisher
    ) {
        this(
                userCouponRepository,
                couponCampaignService,
                couponTemplateService,
                campaignStockCache,
                receiveRequestRepository,
                couponEventPublisher,
                event -> {
                }
        );
    }

    @Autowired
    public UserCouponService(
            UserCouponRepository userCouponRepository,
            CouponCampaignService couponCampaignService,
            CouponTemplateService couponTemplateService,
            CampaignStockCache campaignStockCache,
            ReceiveRequestRepository receiveRequestRepository,
            CouponEventPublisher couponEventPublisher,
            CouponReceiveRequestPublisher couponReceiveRequestPublisher
    ) {
        this.userCouponRepository = userCouponRepository;
        this.couponCampaignService = couponCampaignService;
        this.couponTemplateService = couponTemplateService;
        this.campaignStockCache = campaignStockCache;
        this.receiveRequestRepository = receiveRequestRepository;
        this.couponEventPublisher = couponEventPublisher;
        this.couponReceiveRequestPublisher = couponReceiveRequestPublisher;
    }

    @Transactional
    public UserCoupon receive(ReceiveCouponRequest request) {
        return receiveRequestRepository.findResult(request.requestId())
                .orElseGet(() -> doReceive(request));
    }

    public CouponReceiveAcceptedView receiveByMq(ReceiveCouponRequest request) {
        receiveRequestRepository.findResult(request.requestId())
                .ifPresent(userCoupon -> {
                    throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "领券请求已处理完成，请勿重复提交");
                });
        couponCampaignService.ensureReceivable(request.campaignId());
        String eventId = UUID.randomUUID().toString();
        OffsetDateTime acceptedAt = OffsetDateTime.now();
        couponReceiveRequestPublisher.publish(new CouponReceiveRequestedEvent(
                eventId,
                request.requestId(),
                request.userId(),
                request.campaignId(),
                acceptedAt
        ));
        return new CouponReceiveAcceptedView(
                eventId,
                request.requestId(),
                request.userId(),
                request.campaignId(),
                "ACCEPTED",
                acceptedAt
        );
    }

    private UserCoupon doReceive(ReceiveCouponRequest request) {
        couponCampaignService.ensureReceivable(request.campaignId());
        CouponCampaign campaign = couponCampaignService.getById(request.campaignId());
        validateReceiveLimit(request.userId(), campaign);
        deductStock(request.userId(), campaign);
        try {
            couponCampaignService.deductDatabaseStock(campaign.getId());
            return createUserCoupon(request, campaign);
        } catch (RuntimeException exception) {
            campaignStockCache.restoreStock(campaign.getId(), request.userId());
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
        return userCouponRepository.lock(userCouponId, orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券当前状态不可锁定"));
    }

    @Transactional
    public UserCoupon confirm(Long userId, Long userCouponId, String orderNo) {
        UserCoupon userCoupon = getLockedCoupon(userId, userCouponId, orderNo);
        return userCouponRepository.confirmUse(userCoupon.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券当前状态不是锁定状态"));
    }

    @Transactional
    public UserCoupon cancel(Long userId, Long userCouponId, String orderNo) {
        UserCoupon userCoupon = getLockedCoupon(userId, userCouponId, orderNo);
        return userCouponRepository.release(userCoupon.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券当前状态不是锁定状态"));
    }

    @Transactional
    public UserCoupon refund(Long userId, Long userCouponId, String orderNo) {
        UserCoupon userCoupon = getOwnedCoupon(userId, userCouponId);
        if (userCoupon.getStatus() != UserCouponStatus.USED) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券当前状态不可退款返还");
        }
        if (!orderNo.equals(userCoupon.getOrderNo())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "订单号与核销记录不匹配");
        }
        return userCouponRepository.refund(userCoupon.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券当前状态不可退款返还"));
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

    private void deductStock(Long userId, CouponCampaign campaign) {
        StockDeductResult result = campaignStockCache.tryDeductStock(
                campaign.getId(),
                userId,
                campaign.getPerUserLimit()
        );
        if (result.status() == StockDeductStatus.RECEIVE_LIMIT_EXCEEDED) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "用户已达到该活动领取上限");
        }
        if (!result.success()) {
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
