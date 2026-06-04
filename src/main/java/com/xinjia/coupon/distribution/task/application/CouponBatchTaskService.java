package com.xinjia.coupon.distribution.task.application;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xinjia.coupon.common.api.PageResult;
import com.xinjia.coupon.common.enums.CouponBatchTaskStatus;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTaskFailure;
import com.xinjia.coupon.distribution.task.infrastructure.CouponBatchTaskFailureRepository;
import com.xinjia.coupon.distribution.task.infrastructure.CouponBatchTaskRepository;
import com.xinjia.coupon.distribution.task.web.CreateCouponBatchTaskRequest;
import com.xinjia.coupon.user.coupon.application.UserCouponService;
import com.xinjia.coupon.user.coupon.web.ReceiveCouponRequest;

@Service
public class CouponBatchTaskService {

    private final CouponBatchTaskRepository couponBatchTaskRepository;
    private final CouponBatchTaskFailureRepository couponBatchTaskFailureRepository;
    private final UserCouponService userCouponService;

    public CouponBatchTaskService(
            CouponBatchTaskRepository couponBatchTaskRepository,
            CouponBatchTaskFailureRepository couponBatchTaskFailureRepository,
            UserCouponService userCouponService
    ) {
        this.couponBatchTaskRepository = couponBatchTaskRepository;
        this.couponBatchTaskFailureRepository = couponBatchTaskFailureRepository;
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

        for (int index = 0; index < userIds.size(); index++) {
            Long userId = userIds.get(index);
            try {
                userCouponService.receive(new ReceiveCouponRequest(
                        task.getBatchNo() + "-" + userId,
                        userId,
                        task.getCampaignId()
                ));
                task.recordSuccess();
            } catch (RuntimeException exception) {
                task.recordFailure();
                couponBatchTaskFailureRepository.save(CouponBatchTaskFailure.create(
                        task.getId(),
                        task.getBatchNo(),
                        userId,
                        index + 1,
                        exception.getMessage()
                ));
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

    @Transactional(readOnly = true)
    public PageResult<CouponBatchTask> page(CouponBatchTaskStatus status, int pageNo, int pageSize) {
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);
        return new PageResult<>(
                couponBatchTaskRepository.findPage(status, normalizedPageNo, normalizedPageSize),
                couponBatchTaskRepository.count(status),
                normalizedPageNo,
                normalizedPageSize
        );
    }

    @Transactional(readOnly = true)
    public List<CouponBatchTaskFailure> listFailures(Long taskId) {
        getById(taskId);
        return couponBatchTaskFailureRepository.findByTaskId(taskId);
    }
}
