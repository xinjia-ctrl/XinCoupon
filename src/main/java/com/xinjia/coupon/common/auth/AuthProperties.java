package com.xinjia.coupon.common.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xincoupon.auth")
public class AuthProperties {

    private boolean enabled = false;
    private String userIdHeader = "X-User-Id";
    private String adminTokenHeader = "X-Admin-Token";
    private String adminToken = "xin-coupon-admin";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserIdHeader() {
        return userIdHeader;
    }

    public void setUserIdHeader(String userIdHeader) {
        this.userIdHeader = userIdHeader;
    }

    public String getAdminTokenHeader() {
        return adminTokenHeader;
    }

    public void setAdminTokenHeader(String adminTokenHeader) {
        this.adminTokenHeader = adminTokenHeader;
    }

    public String getAdminToken() {
        return adminToken;
    }

    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }
}
