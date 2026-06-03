package com.xinjia.coupon.search.infrastructure;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;

@Repository
public class InMemoryCouponTemplateSearchIndex implements CouponTemplateSearchIndex {

    private final ConcurrentMap<Long, CouponTemplateSearchDocument> documents = new ConcurrentHashMap<>();

    @Override
    public void save(CouponTemplateSearchDocument document) {
        documents.put(document.templateId(), document);
    }

    @Override
    public void replaceAll(Collection<CouponTemplateSearchDocument> newDocuments) {
        documents.clear();
        newDocuments.forEach(this::save);
    }

    @Override
    public List<CouponTemplateSearchDocument> search(String keyword, Long merchantId, CouponTemplateStatus status) {
        String normalizedKeyword = normalize(keyword);
        return documents.values()
                .stream()
                .filter(document -> merchantId == null || merchantId.equals(document.merchantId()))
                .filter(document -> status == null || status == document.status())
                .filter(document -> !StringUtils.hasText(normalizedKeyword)
                        || document.title().toLowerCase(Locale.ROOT).contains(normalizedKeyword))
                .sorted(Comparator.comparing(CouponTemplateSearchDocument::updatedAt).reversed())
                .toList();
    }

    private String normalize(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.toLowerCase(Locale.ROOT) : null;
    }
}
