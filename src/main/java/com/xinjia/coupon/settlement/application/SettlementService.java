package com.xinjia.coupon.settlement.application;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.settlement.domain.CouponSettlement;
import com.xinjia.coupon.settlement.infrastructure.CouponSettlementRepository;
import com.xinjia.coupon.settlement.web.SettlementCalculateRequest;
import com.xinjia.coupon.settlement.web.SettlementCalculateView;
import com.xinjia.coupon.settlement.web.AvailableCouponView;
import com.xinjia.coupon.settlement.web.CouponCancelRequest;
import com.xinjia.coupon.settlement.web.CouponConfirmRequest;
import com.xinjia.coupon.settlement.web.CouponLockRequest;
import com.xinjia.coupon.settlement.web.CouponOperationView;
import com.xinjia.coupon.settlement.web.CouponRefundRequest;
import com.xinjia.coupon.user.coupon.application.UserCouponService;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;

@Service
public class SettlementService {

    private final UserCouponService userCouponService;
    private final CouponTemplateService couponTemplateService;
    private final CouponSettlementRepository couponSettlementRepository;

    public SettlementService(
            UserCouponService userCouponService,
            CouponTemplateService couponTemplateService,
            CouponSettlementRepository couponSettlementRepository
    ) {
        this.userCouponService = userCouponService;
        this.couponTemplateService = couponTemplateService;
        this.couponSettlementRepository = couponSettlementRepository;
    }

    @Transactional(readOnly = true)
    public SettlementCalculateView calculate(SettlementCalculateRequest request) {
        List<AvailableCouponView> availableCoupons = findAvailableCoupons(request);
        AvailableCouponView bestCoupon = selectBestCoupon(availableCoupons).orElse(null);
        Long bestDiscountAmount = bestCoupon == null ? 0L : bestCoupon.calculatedDiscountAmount();
        return new SettlementCalculateView(
                request.userId(),
                request.orderNo(),
                request.merchantId(),
                request.orderAmount(),
                bestDiscountAmount,
                Math.max(request.orderAmount() - bestDiscountAmount, 0L),
                availableCoupons,
                bestCoupon
        );
    }

    @Transactional
    public CouponOperationView lock(CouponLockRequest request) {
        if (couponSettlementRepository.existsActive(request.userId(), request.userCouponId())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券已存在未完结结算单");
        }
        UserCoupon userCoupon = userCouponService.lock(request.userId(), request.userCouponId(), request.orderNo());
        couponSettlementRepository.save(CouponSettlement.lock(
                request.userId(),
                request.userCouponId(),
                request.orderNo()
        ));
        return CouponOperationView.from(userCoupon);
    }

    @Transactional
    public CouponOperationView confirm(CouponConfirmRequest request) {
        CouponSettlement settlement = couponSettlementRepository
                .findLocked(request.userId(), request.userCouponId(), request.orderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券结算单不是锁定状态"));
        UserCoupon userCoupon = userCouponService.confirm(request.userId(), request.userCouponId(), request.orderNo());
        settlement.markPaid();
        couponSettlementRepository.save(settlement);
        return CouponOperationView.from(userCoupon);
    }

    @Transactional
    public CouponOperationView cancel(CouponCancelRequest request) {
        CouponSettlement settlement = couponSettlementRepository
                .findLocked(request.userId(), request.userCouponId(), request.orderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券结算单不是锁定状态"));
        UserCoupon userCoupon = userCouponService.cancel(request.userId(), request.userCouponId(), request.orderNo());
        settlement.cancel();
        couponSettlementRepository.save(settlement);
        return CouponOperationView.from(userCoupon);
    }

    @Transactional
    public CouponOperationView refund(CouponRefundRequest request) {
        CouponSettlement settlement = couponSettlementRepository
                .findPaid(request.userId(), request.userCouponId(), request.orderNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券结算单不是已支付状态"));
        UserCoupon userCoupon = userCouponService.refund(request.userId(), request.userCouponId(), request.orderNo());
        settlement.refund();
        couponSettlementRepository.save(settlement);
        return CouponOperationView.from(userCoupon);
    }

    private List<AvailableCouponView> findAvailableCoupons(SettlementCalculateRequest request) {
        return userCouponService.listByUserIdAndStatus(request.userId(), UserCouponStatus.RECEIVED)
                .stream()
                .filter(userCoupon -> userCoupon.getExpiredAt().isAfter(OffsetDateTime.now()))
                .map(userCoupon -> toAvailableCoupon(request, userCoupon))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<AvailableCouponView> toAvailableCoupon(
            SettlementCalculateRequest request,
            UserCoupon userCoupon
    ) {
        CouponTemplate template = couponTemplateService.getById(userCoupon.getTemplateId());
        if (!template.getMerchantId().equals(request.merchantId())) {
            return Optional.empty();
        }
        if (request.orderAmount() < template.getThresholdAmount()) {
            return Optional.empty();
        }
        Long calculatedDiscountAmount = calculateDiscountAmount(request.orderAmount(), template);
        if (calculatedDiscountAmount <= 0) {
            return Optional.empty();
        }
        return Optional.of(new AvailableCouponView(
                userCoupon.getId(),
                template.getId(),
                userCoupon.getCouponCode(),
                template.getTitle(),
                template.getCouponType(),
                template.getThresholdAmount(),
                template.getDiscountAmount(),
                template.getDiscountRate(),
                calculatedDiscountAmount,
                userCoupon.getExpiredAt()
        ));
    }

    private Optional<AvailableCouponView> selectBestCoupon(List<AvailableCouponView> availableCoupons) {
        return availableCoupons.stream()
                .max(Comparator.comparing(AvailableCouponView::calculatedDiscountAmount));
    }

    private Long calculateDiscountAmount(Long orderAmount, CouponTemplate template) {
        Long discountAmount = switch (template.getCouponType()) {
            case FULL_REDUCTION, CASH -> template.getDiscountAmount();
            case DISCOUNT -> calculateRateDiscount(orderAmount, template.getDiscountRate());
        };
        if (discountAmount == null || discountAmount < 0) {
            return 0L;
        }
        return Math.min(orderAmount, discountAmount);
    }

    private Long calculateRateDiscount(Long orderAmount, Integer discountRate) {
        if (discountRate == null || discountRate <= 0 || discountRate >= 100) {
            return 0L;
        }
        return orderAmount - orderAmount * discountRate / 100;
    }
}
