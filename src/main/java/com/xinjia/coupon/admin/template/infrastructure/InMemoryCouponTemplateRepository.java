package com.xinjia.coupon.admin.template.infrastructure;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;

@Repository
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
}
