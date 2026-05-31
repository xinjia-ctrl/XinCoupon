package com.xinjia.coupon.admin.template.infrastructure.persistence;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinjia.coupon.common.persistence.BaseAuditEntity;

@TableName("coupon_template")
public class CouponTemplateDO extends BaseAuditEntity {

    private Long merchantId;
    private String title;
    private String couponType;
    private Long discountAmount;
    private Integer discountRate;
    private Long thresholdAmount;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private Integer totalStock;
    private String status;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCouponType() {
        return couponType;
    }

    public void setCouponType(String couponType) {
        this.couponType = couponType;
    }

    public Long getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Long discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Integer discountRate) {
        this.discountRate = discountRate;
    }

    public Long getThresholdAmount() {
        return thresholdAmount;
    }

    public void setThresholdAmount(Long thresholdAmount) {
        this.thresholdAmount = thresholdAmount;
    }

    public LocalDateTime getValidStartTime() {
        return validStartTime;
    }

    public void setValidStartTime(LocalDateTime validStartTime) {
        this.validStartTime = validStartTime;
    }

    public LocalDateTime getValidEndTime() {
        return validEndTime;
    }

    public void setValidEndTime(LocalDateTime validEndTime) {
        this.validEndTime = validEndTime;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
