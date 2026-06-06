package com.xinjia.coupon.distribution.task.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.user.coupon.infrastructure.StockDeductStatus;

@Component
@ConditionalOnMissingBean(CouponBatchUserStagingStore.class)
public class InMemoryCouponBatchUserStagingStore implements CouponBatchUserStagingStore {

    private final ConcurrentMap<Long, Set<String>> stagedRows = new ConcurrentHashMap<>();

    @Override
    public BatchStageResult stage(Long taskId, Long campaignId, BatchUserRow row, Integer perUserLimit) {
        Set<String> rows = stagedRows.computeIfAbsent(taskId, ignored -> ConcurrentHashMap.newKeySet());
        rows.add(encode(row));
        return new BatchStageResult(StockDeductStatus.SUCCESS, rows.size());
    }

    @Override
    public List<BatchUserRow> pop(Long taskId, int count) {
        Set<String> rows = stagedRows.get(taskId);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<BatchUserRow> popped = new ArrayList<>(Math.min(count, rows.size()));
        for (String value : new ArrayList<>(rows)) {
            if (popped.size() >= count) {
                break;
            }
            if (rows.remove(value)) {
                popped.add(decode(value));
            }
        }
        return popped;
    }

    @Override
    public long size(Long taskId) {
        Set<String> rows = stagedRows.get(taskId);
        return rows == null ? 0 : rows.size();
    }

    @Override
    public void restoreReservation(Long campaignId, Long userId) {
        // 内存暂存实现不维护库存镜像，库存一致性由数据库批量扣减兜底。
    }

    @Override
    public void clear(Long taskId) {
        stagedRows.remove(taskId);
    }

    private String encode(BatchUserRow row) {
        return row.userId() + ":" + row.rowNumber();
    }

    private BatchUserRow decode(String value) {
        String[] parts = value.split(":", 2);
        return new BatchUserRow(Long.valueOf(parts[0]), Integer.parseInt(parts[1]));
    }
}
