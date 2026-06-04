package com.xinjia.coupon.admin.template.infrastructure;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.enums.CouponTemplateStatus;

public class InMemoryCouponTemplateRepository implements CouponTemplateRepository {

    private final AtomicLong idGenerator = new AtomicLong(1000);
    private final ConcurrentMap<Long, CouponTemplate> templates = new ConcurrentHashMap<>();

    @Override
    public CouponTemplate save(CouponTemplate template) {
        if (template.getId() == null) {
            template.assignId(idGenerator.incrementAndGet());
        }
        templates.put(template.getId(), template);
        return template;
    }

    @Override
    public Optional<CouponTemplate> findById(Long id) {
        return Optional.ofNullable(templates.get(id));
    }

    @Override
    public List<CouponTemplate> findAll() {
        return templates.values()
                .stream()
                .sorted(Comparator.comparing(CouponTemplate::getId).reversed())
                .toList();
    }

    @Override
    public Optional<CouponTemplate> updateStatus(Long id, CouponTemplateStatus status) {
        return findById(id)
                .map(template -> {
                    template.changeStatus(status);
                    return template;
                });
    }

    @Override
    public Optional<CouponTemplate> increaseStock(Long id, Integer increasedStock) {
        return findById(id)
                .map(template -> {
                    template.increaseStock(increasedStock);
                    return template;
                });
    }
}
