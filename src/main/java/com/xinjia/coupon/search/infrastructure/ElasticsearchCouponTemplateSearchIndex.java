package com.xinjia.coupon.search.infrastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;
import com.xinjia.coupon.search.sync.SearchSyncProperties;

@Repository
@ConditionalOnProperty(name = "xincoupon.search.index-type", havingValue = "ELASTICSEARCH")
public class ElasticsearchCouponTemplateSearchIndex implements CouponTemplateSearchIndex {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final SearchSyncProperties searchSyncProperties;

    public ElasticsearchCouponTemplateSearchIndex(
            ObjectMapper objectMapper,
            SearchSyncProperties searchSyncProperties
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(searchSyncProperties.getElasticsearch().getUrl())
                .build();
        this.objectMapper = objectMapper;
        this.searchSyncProperties = searchSyncProperties;
    }

    @Override
    public void save(CouponTemplateSearchDocument document) {
        restClient.put()
                .uri("/{indexName}/_doc/{id}", indexName(), document.templateId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(document)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void delete(Long templateId) {
        restClient.delete()
                .uri("/{indexName}/_doc/{id}", indexName(), templateId)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void replaceAll(Collection<CouponTemplateSearchDocument> documents) {
        for (CouponTemplateSearchDocument document : documents) {
            save(document);
        }
    }

    @Override
    public List<CouponTemplateSearchDocument> search(String keyword, Long merchantId, CouponTemplateStatus status) {
        Map<String, Object> body = Map.of(
                "query", buildQuery(keyword, merchantId, status),
                "sort", List.of(Map.of("updatedAt", Map.of("order", "desc")))
        );
        JsonNode response = restClient.post()
                .uri("/{indexName}/_search", indexName())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (response == null) {
            return List.of();
        }
        List<CouponTemplateSearchDocument> documents = new ArrayList<>();
        for (JsonNode hit : response.path("hits").path("hits")) {
            documents.add(objectMapper.convertValue(hit.path("_source"), CouponTemplateSearchDocument.class));
        }
        return documents;
    }

    private Map<String, Object> buildQuery(String keyword, Long merchantId, CouponTemplateStatus status) {
        List<Map<String, Object>> must = new ArrayList<>();
        List<Map<String, Object>> filter = new ArrayList<>();
        if (StringUtils.hasText(keyword)) {
            must.add(Map.of("match_phrase", Map.of("title", keyword)));
        }
        if (merchantId != null) {
            filter.add(Map.of("term", Map.of("merchantId", merchantId)));
        }
        if (status != null) {
            filter.add(Map.of("term", Map.of("status.keyword", status.name())));
        }
        if (must.isEmpty() && filter.isEmpty()) {
            return Map.of("match_all", Map.of());
        }
        return Map.of("bool", Map.of("must", must, "filter", filter));
    }

    private String indexName() {
        return searchSyncProperties.getElasticsearch().getIndexName();
    }
}
