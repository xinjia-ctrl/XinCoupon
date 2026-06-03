package com.xinjia.coupon.distribution.task.application;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;
import com.xinjia.coupon.distribution.task.infrastructure.CouponBatchTaskRepository;
import com.xinjia.coupon.distribution.task.web.CreateCouponBatchTaskRequest;
import com.xinjia.coupon.user.coupon.application.UserCouponService;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

@Service
public class CouponBatchTaskService {

    private final CouponBatchTaskRepository couponBatchTaskRepository;
    private final UserCouponService userCouponService;

    public CouponBatchTaskService(
            CouponBatchTaskRepository couponBatchTaskRepository,
            UserCouponService userCouponService
    ) {
        this.couponBatchTaskRepository = couponBatchTaskRepository;
        this.userCouponService = userCouponService;
    }

    @Transactional
    public CouponBatchTask create(CreateCouponBatchTaskRequest request) {
        couponBatchTaskRepository.findByBatchNo(request.batchNo())
                .ifPresent(task -> {
                    throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "批量发券任务编号已存在");
                });
        CouponBatchTask task = CouponBatchTask.create(
                request.batchNo(),
                request.campaignId(),
                request.userIds().size()
        );
        return couponBatchTaskRepository.save(task);
    }

    public void dispatchAsync(Long taskId, List<Long> userIds) {
        CompletableFuture.runAsync(() -> execute(taskId, userIds));
    }

    @Transactional
    public CouponBatchTask execute(Long taskId, List<Long> userIds) {
        CouponBatchTask task = getById(taskId);
        task.markRunning();
        couponBatchTaskRepository.save(task);

        for (Long userId : userIds) {
            try {
                userCouponService.receive(new ReceiveCouponRequest(
                        task.getBatchNo() + "-" + userId,
                        userId,
                        task.getCampaignId()
                ));
                task.recordSuccess();
            } catch (RuntimeException exception) {
                task.recordFailure();
            }
            couponBatchTaskRepository.save(task);
        }
        task.complete();
        return couponBatchTaskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public CouponBatchTask getById(Long taskId) {
        return couponBatchTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "批量发券任务不存在"));
    }
}
