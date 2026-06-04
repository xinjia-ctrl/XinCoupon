package com.xinjia.coupon.admin.template.application;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.admin.template.infrastructure.CouponTemplateBloomFilter;
import com.xinjia.coupon.admin.template.infrastructure.CouponTemplateRepository;
import com.xinjia.coupon.admin.template.web.CreateCouponTemplateRequest;
import com.xinjia.coupon.admin.template.web.IncreaseCouponTemplateStockRequest;
import com.xinjia.coupon.admin.template.web.UpdateCouponTemplateStatusRequest;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;

@Service
public class CouponTemplateService {

    private final CouponTemplateRepository couponTemplateRepository;
    private final CouponTemplateChangePublisher couponTemplateChangePublisher;
    private final CouponTemplateBloomFilter couponTemplateBloomFilter;

    public CouponTemplateService(CouponTemplateRepository couponTemplateRepository) {
        this(
                couponTemplateRepository,
                CouponTemplateChangePublisher.noop(),
                CouponTemplateBloomFilter.alwaysMaybe()
        );
    }

    @Autowired
    public CouponTemplateService(
            CouponTemplateRepository couponTemplateRepository,
            CouponTemplateChangePublisher couponTemplateChangePublisher,
            CouponTemplateBloomFilter couponTemplateBloomFilter
    ) {
        this.couponTemplateRepository = couponTemplateRepository;
        this.couponTemplateChangePublisher = couponTemplateChangePublisher;
        this.couponTemplateBloomFilter = couponTemplateBloomFilter;
    }

    @Transactional
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
        CouponTemplate saved = couponTemplateRepository.save(template);
        couponTemplateBloomFilter.put(saved.getId());
        couponTemplateChangePublisher.publish(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public CouponTemplate getById(Long templateId) {
        if (!couponTemplateBloomFilter.mightContain(templateId)) {
            return findByIdAndBackfillBloomFilter(templateId);
        }
        return findByIdAndBackfillBloomFilter(templateId);
    }

    private CouponTemplate findByIdAndBackfillBloomFilter(Long templateId) {
        return couponTemplateRepository.findById(templateId)
                .map(template -> {
                    couponTemplateBloomFilter.put(template.getId());
                    return template;
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券模板不存在"));
    }

    @Transactional(readOnly = true)
    public List<CouponTemplate> list() {
        return couponTemplateRepository.findAll();
    }

    @Transactional
    public CouponTemplate changeStatus(Long templateId, UpdateCouponTemplateStatusRequest request) {
        CouponTemplate changed = couponTemplateRepository.updateStatus(templateId, request.status())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券模板不存在"));
        couponTemplateChangePublisher.publish(changed);
        return changed;
    }

    @Transactional
    public CouponTemplate increaseStock(Long templateId, IncreaseCouponTemplateStockRequest request) {
        CouponTemplate template = getById(templateId);
        if (template.getStatus() == CouponTemplateStatus.DISABLED || template.getStatus() == CouponTemplateStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "已停用或过期的优惠券模板不可增加发行量");
        }
        CouponTemplate changed = couponTemplateRepository.increaseStock(templateId, request.increasedStock())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券模板不存在"));
        couponTemplateChangePublisher.publish(changed);
        return changed;
    }

    @Transactional
    public CouponTemplate terminate(Long templateId) {
        CouponTemplate template = getById(templateId);
        if (template.getStatus() == CouponTemplateStatus.DISABLED) {
            return template;
        }
        CouponTemplate changed = couponTemplateRepository.updateStatus(templateId, CouponTemplateStatus.DISABLED)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券模板不存在"));
        couponTemplateChangePublisher.publish(changed);
        return changed;
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
