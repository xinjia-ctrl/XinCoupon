package com.xinjia.coupon.engine.remind.application;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xinjia.coupon.admin.template.application.CouponTemplateService;
import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.CouponTemplateRemindStatus;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.engine.remind.domain.CouponTemplateRemind;
import com.xinjia.coupon.engine.remind.infrastructure.CouponTemplateRemindRepository;
import com.xinjia.coupon.engine.remind.web.CancelCouponTemplateRemindRequest;
import com.xinjia.coupon.engine.remind.web.CreateCouponTemplateRemindRequest;

@Service
public class CouponTemplateRemindService {

    private final CouponTemplateRemindRepository couponTemplateRemindRepository;
    private final CouponTemplateService couponTemplateService;

    public CouponTemplateRemindService(
            CouponTemplateRemindRepository couponTemplateRemindRepository,
            CouponTemplateService couponTemplateService
    ) {
        this.couponTemplateRemindRepository = couponTemplateRemindRepository;
        this.couponTemplateService = couponTemplateService;
    }

    @Transactional
    public CouponTemplateRemind create(CreateCouponTemplateRemindRequest request) {
        CouponTemplate template = couponTemplateService.getById(request.templateId());
        if (!request.remindAt().isBefore(template.getValidStartTime())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "提醒时间必须早于优惠券有效开始时间");
        }
        couponTemplateRemindRepository.findActiveByUserIdAndTemplateId(request.userId(), request.templateId())
                .ifPresent(remind -> {
                    throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "该优惠券模板已存在有效预约提醒");
                });
        return couponTemplateRemindRepository.save(CouponTemplateRemind.create(
                request.userId(),
                request.templateId(),
                request.remindType(),
                request.remindAt()
        ));
    }

    @Transactional(readOnly = true)
    public List<CouponTemplateRemind> list(Long userId, CouponTemplateRemindStatus status) {
        return couponTemplateRemindRepository.findByUserId(userId, status);
    }

    @Transactional
    public CouponTemplateRemind cancel(CancelCouponTemplateRemindRequest request) {
        CouponTemplateRemind remind = couponTemplateRemindRepository.findById(request.remindId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "优惠券预约提醒不存在"));
        if (!remind.getUserId().equals(request.userId())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券预约提醒归属不匹配");
        }
        if (remind.getStatus() == CouponTemplateRemindStatus.CANCELED) {
            return remind;
        }
        if (remind.getRemindAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "已触发的预约提醒不可取消");
        }
        remind.cancel();
        return couponTemplateRemindRepository.save(remind);
    }
}
