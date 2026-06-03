package com.xinjia.coupon.admin.template.infrastructure;

public interface CouponTemplateBloomFilter {

    void put(Long templateId);

    boolean mightContain(Long templateId);

    static CouponTemplateBloomFilter alwaysMaybe() {
        return new CouponTemplateBloomFilter() {
            @Override
            public void put(Long templateId) {
            }

            @Override
            public boolean mightContain(Long templateId) {
                return true;
            }
        };
    }
}
