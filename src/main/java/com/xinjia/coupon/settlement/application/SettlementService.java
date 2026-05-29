package com.xinjia.coupon.settlement.application;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.UserCouponStatus;
import com.xinjia.coupon.settlement.web.SettlementCalculateRequest;
import com.xinjia.coupon.settlement.web.SettlementCalculateView;
import com.xinjia.coupon.settlement.web.AvailableCouponView;
import com.xinjia.coupon.user.coupon.application.UserCouponService;
import com.xinjia.coupon.user.coupon.domain.UserCoupon;

@Service
public class SettlementService {

    private final UserCouponService userCouponService;
    private final CouponTemplateService couponTemplateService;

    public SettlementService(UserCouponService userCouponService, CouponTemplateService couponTemplateService) {
        this.userCouponService = userCouponService;
        this.couponTemplateService = couponTemplateService;
    }

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

    private List<AvailableCouponView> findAvailableCoupons(SettlementCalculateRequest request) {
        return userCouponService.listByUserId(request.userId())
                .stream()
                .filter(userCoupon -> userCoupon.getStatus() == UserCouponStatus.RECEIVED)
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
