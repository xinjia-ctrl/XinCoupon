package com.xinjia.coupon.admin.template.infrastructure;

import java.time.Duration;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.admin.template.domain.CouponTemplate;
import com.xinjia.coupon.common.config.RedisCacheProperties;
import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;

@Component
@ConditionalOnProperty(name = "xincoupon.template.cache.store-type", havingValue = "redis")
public class RedisCouponTemplateCache implements CouponTemplateCache {

    private static final String TEMPLATE_CACHE_KEY = "coupon-template:";
    private static final String NULL_VALUE = "__NULL__";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisCacheProperties redisCacheProperties;

    public RedisCouponTemplateCache(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            RedisCacheProperties redisCacheProperties
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.redisCacheProperties = redisCacheProperties;
    }

    @Override
    public Optional<CouponTemplate> get(Long templateId) {
        String value = stringRedisTemplate.opsForValue().get(buildKey(templateId));
        if (value == null || NULL_VALUE.equals(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, CouponTemplate.class));
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券模板缓存反序列化失败");
        }
    }

    @Override
    public boolean isNullValue(Long templateId) {
        return NULL_VALUE.equals(stringRedisTemplate.opsForValue().get(buildKey(templateId)));
    }

    @Override
    public void put(CouponTemplate template, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(
                    buildKey(template.getId()),
                    objectMapper.writeValueAsString(template),
                    ttl
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, "优惠券模板缓存序列化失败");
        }
    }

    @Override
    public void putNull(Long templateId, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildKey(templateId), NULL_VALUE, ttl);
    }

    private String buildKey(Long templateId) {
        return redisCacheProperties.buildKey(TEMPLATE_CACHE_KEY + templateId);
    }
}
