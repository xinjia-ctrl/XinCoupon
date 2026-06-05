package com.xinjia.coupon.admin.template.infrastructure;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;

@Component
@ConditionalOnMissingBean(CouponTemplateCache.class)
public class InMemoryCouponTemplateCache implements CouponTemplateCache {

    private final ConcurrentMap<Long, Entry> entries = new ConcurrentHashMap<>();

    @Override
    public Optional<CouponTemplate> get(Long templateId) {
        Entry entry = getValidEntry(templateId);
        if (entry == null || entry.nullValue()) {
            return Optional.empty();
        }
        return Optional.of(entry.template());
    }

    @Override
    public boolean isNullValue(Long templateId) {
        Entry entry = getValidEntry(templateId);
        return entry != null && entry.nullValue();
    }

    @Override
    public void put(CouponTemplate template, Duration ttl) {
        entries.put(template.getId(), Entry.template(template, ttl));
    }

    @Override
    public void putNull(Long templateId, Duration ttl) {
        entries.put(templateId, Entry.nullValue(ttl));
    }

    private Entry getValidEntry(Long templateId) {
        Entry entry = entries.get(templateId);
        if (entry == null) {
            return null;
        }
        if (!entry.expiredAt().isAfter(Instant.now())) {
            entries.remove(templateId);
            return null;
        }
        return entry;
    }

    private record Entry(CouponTemplate template, boolean nullValue, Instant expiredAt) {

        static Entry template(CouponTemplate template, Duration ttl) {
            return new Entry(template, false, Instant.now().plus(ttl));
        }

        static Entry nullValue(Duration ttl) {
            return new Entry(null, true, Instant.now().plus(ttl));
        }
    }
}
