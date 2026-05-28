package com.xinjia.coupon.admin.template.application;

import org.springframework.stereotype.Service;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.admin.template.infrastructure.CouponTemplateRepository;
import com.xinjia.coupon.admin.template.web.CreateCouponTemplateRequest;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;

@Service
public class CouponTemplateService {

    private final CouponTemplateRepository couponTemplateRepository;

    public CouponTemplateService(CouponTemplateRepository couponTemplateRepository) {
        this.couponTemplateRepository = couponTemplateRepository;
    }

    public CouponTemplate create(CreateCouponTemplateRequest request) {
        validateTimeRange(request);
        validateDiscountRule(request);

        CouponTemplate template = CouponTemplate.create(
                request.merchantId(),
                request.title(),
                request.couponType(),
                request.discountAmount(),
                request.discountRate(),
                request.thresholdAmount(),
                request.validStartTime(),
                request.validEndTime(),
                request.totalStock()
        );
        return couponTemplateRepository.save(template);
    }

    private void validateTimeRange(CreateCouponTemplateRequest request) {
        if (!request.validEndTime().isAfter(request.validStartTime())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券有效结束时间必须晚于开始时间");
        }
    }

    private void validateDiscountRule(CreateCouponTemplateRequest request) {
        if (request.couponType() == CouponType.DISCOUNT) {
            if (request.discountRate() == null) {
                throw new BusinessException(ErrorCode.PARAMETER_INVALID, "折扣券必须填写折扣比例");
            }
            return;
        }

        if (request.discountAmount() == null || request.discountAmount() <= 0) {
            throw new BusinessException(ErrorCode.PARAMETER_INVALID, "满减券和立减券必须填写优惠金额");
        }
    }
}
