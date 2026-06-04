package com.xinjia.coupon.engine.remind.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.xinjia.coupon.common.enums.CouponTemplateRemindStatus;
import com.xinjia.coupon.engine.remind.domain.CouponTemplateRemind;

@Repository
public class InMemoryCouponTemplateRemindRepository implements CouponTemplateRemindRepository {

    private final AtomicLong idGenerator = new AtomicLong(9000);
    private final ConcurrentMap<Long, CouponTemplateRemind> reminds = new ConcurrentHashMap<>();

    @Override
    public CouponTemplateRemind save(CouponTemplateRemind remind) {
        if (remind.getId() == null) {
            remind.assignId(idGenerator.incrementAndGet());
        }
        reminds.put(remind.getId(), remind);
        return remind;
    }

    @Override
    public Optional<CouponTemplateRemind> findById(Long id) {
        return Optional.ofNullable(reminds.get(id));
    }

    @Override
    public Optional<CouponTemplateRemind> findActiveByUserIdAndTemplateId(Long userId, Long templateId) {
        return reminds.values()
                .stream()
                .filter(remind -> remind.getUserId().equals(userId))
                .filter(remind -> remind.getTemplateId().equals(templateId))
                .filter(remind -> remind.getStatus() == CouponTemplateRemindStatus.ACTIVE)
                .findFirst();
    }

    @Override
    public List<CouponTemplateRemind> findByUserId(Long userId, CouponTemplateRemindStatus status) {
        return reminds.values()
                .stream()
                .filter(remind -> remind.getUserId().equals(userId))
                .filter(remind -> status == null || remind.getStatus() == status)
                .sorted(Comparator.comparing(CouponTemplateRemind::getRemindAt))
                .toList();
    }
}
