package com.xinjia.coupon.search.sync;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

import com.xinjia.coupon.common.enums.CouponTemplateStatus;
import com.xinjia.coupon.common.enums.CouponType;
import com.xinjia.coupon.search.domain.CouponTemplateSearchDocument;

public class CanalCouponTemplateDocumentMapper {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public CouponTemplateSearchDocument fromRow(Map<String, String> row) {
        return new CouponTemplateSearchDocument(
                longValue(row, "id"),
                longValue(row, "merchant_id"),
                row.get("title"),
                enumValue(CouponType.class, row.get("coupon_type")),
                nullableLong(row, "threshold_amount"),
                nullableLong(row, "discount_amount"),
                nullableInteger(row, "discount_rate"),
                enumValue(CouponTemplateStatus.class, row.get("status")),
                dateTime(row.get("valid_start_time")),
                dateTime(row.get("valid_end_time")),
                dateTime(row.get("updated_at"))
        );
    }

    private Long longValue(Map<String, String> row, String key) {
        return Long.valueOf(row.get(key));
    }

    private Long nullableLong(Map<String, String> row, String key) {
        String value = row.get(key);
        return value == null || value.isBlank() ? null : Long.valueOf(value);
    }

    private Integer nullableInteger(Map<String, String> row, String key) {
        String value = row.get(key);
        return value == null || value.isBlank() ? null : Integer.valueOf(value);
    }

    private <T extends Enum<T>> T enumValue(Class<T> enumType, String value) {
        return Enum.valueOf(enumType, value);
    }

    private OffsetDateTime dateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value.replace(" ", "T")).atZone(ZONE_ID).toOffsetDateTime();
    }
}
