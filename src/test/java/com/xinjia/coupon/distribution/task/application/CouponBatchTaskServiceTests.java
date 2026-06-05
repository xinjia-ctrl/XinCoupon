package com.xinjia.coupon.distribution.task.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.xinjia.coupon.common.enums.CouponBatchTaskStatus;
import com.xinjia.coupon.common.exception.BusinessException;
import com.xinjia.coupon.distribution.task.domain.CouponBatchTask;
import com.xinjia.coupon.distribution.task.infrastructure.InMemoryCouponBatchTaskFailureRepository;
import com.xinjia.coupon.distribution.task.infrastructure.InMemoryCouponBatchTaskRepository;
import com.xinjia.coupon.distribution.task.web.CreateCouponBatchTaskRequest;
import com.xinjia.coupon.user.coupon.application.UserCouponService;

class CouponBatchTaskServiceTests {

    private CouponBatchTaskService couponBatchTaskService;
    private UserCouponService userCouponService;

    @BeforeEach
    void setUp() {
        userCouponService = mock(UserCouponService.class);
        couponBatchTaskService = new CouponBatchTaskService(
                new InMemoryCouponBatchTaskRepository(),
                new InMemoryCouponBatchTaskFailureRepository(),
                userCouponService
        );
    }

    @Test
    void executeShouldCompleteTaskWhenAllUsersReceiveSuccessfully() {
        CouponBatchTask task = couponBatchTaskService.create(request("batch-1", List.of(10L, 11L)));

        CouponBatchTask executed = couponBatchTaskService.execute(task.getId(), List.of(10L, 11L));

        assertThat(executed.getStatus()).isEqualTo(CouponBatchTaskStatus.COMPLETED);
        assertThat(executed.getSuccessCount()).isEqualTo(2);
        assertThat(executed.getFailureCount()).isZero();
    }

    @Test
    void executeShouldRecordPartialFailedTask() {
        doThrow(new BusinessException(com.xinjia.coupon.common.enums.ErrorCode.BUSINESS_REJECTED, "领取失败"))
                .when(userCouponService)
                .receive(any());
        CouponBatchTask task = couponBatchTaskService.create(request("batch-2", List.of(10L)));

        CouponBatchTask executed = couponBatchTaskService.execute(task.getId(), List.of(10L));

        assertThat(executed.getStatus()).isEqualTo(CouponBatchTaskStatus.FAILED);
        assertThat(executed.getSuccessCount()).isZero();
        assertThat(executed.getFailureCount()).isEqualTo(1);
        assertThat(couponBatchTaskService.listFailures(task.getId())).hasSize(1);
    }

    @Test
    void createShouldRejectDuplicatedBatchNo() {
        couponBatchTaskService.create(request("batch-3", List.of(10L)));

        assertThatThrownBy(() -> couponBatchTaskService.create(request("batch-3", List.of(11L))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("批量发券任务编号已存在");
    }

    @Test
    void executeShouldProcessUsersWithFiveThousandBatchSize() {
        List<Long> userIds = LongStream.rangeClosed(1, CouponBatchTaskService.BATCH_SIZE + 1L)
                .boxed()
                .toList();
        CouponBatchTask task = couponBatchTaskService.create(request("batch-4", userIds));

        CouponBatchTask executed = couponBatchTaskService.execute(task.getId(), userIds);

        assertThat(executed.getStatus()).isEqualTo(CouponBatchTaskStatus.COMPLETED);
        assertThat(executed.getSuccessCount()).isEqualTo(CouponBatchTaskService.BATCH_SIZE + 1);
        verify(userCouponService, times(CouponBatchTaskService.BATCH_SIZE + 1)).receive(any());
    }

    @Test
    void exportFailuresShouldWriteExcelContent() {
        doThrow(new BusinessException(com.xinjia.coupon.common.enums.ErrorCode.BUSINESS_REJECTED, "领取失败"))
                .when(userCouponService)
                .receive(any());
        CouponBatchTask task = couponBatchTaskService.create(request("batch-5", List.of(10L)));
        couponBatchTaskService.execute(task.getId(), List.of(10L));

        byte[] content = couponBatchTaskService.exportFailures(task.getId());

        assertThat(content).startsWith(new byte[] {'P', 'K'});
    }

    private CreateCouponBatchTaskRequest request(String batchNo, List<Long> userIds) {
        return new CreateCouponBatchTaskRequest(batchNo, 2001L, userIds);
    }
}
