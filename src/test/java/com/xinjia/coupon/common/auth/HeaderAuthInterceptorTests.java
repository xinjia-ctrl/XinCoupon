package com.xinjia.coupon.common.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.xinjia.coupon.common.exception.BusinessException;

class HeaderAuthInterceptorTests {

    private final AuthProperties authProperties = new AuthProperties();
    private final HeaderAuthInterceptor interceptor = new HeaderAuthInterceptor(authProperties);

    @AfterEach
    void clearIdentity() {
        RequestIdentityHolder.clear();
    }

    @Test
    void shouldSkipAuthenticationWhenDisabled() {
        MockHttpServletRequest request = request("/api/user/coupons");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertTrue(RequestIdentityHolder.get().isEmpty());
    }

    @Test
    void shouldAuthenticateUserByHeader() {
        authProperties.setEnabled(true);
        MockHttpServletRequest request = request("/api/user/coupons");
        request.addHeader("X-User-Id", "1001");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        assertEquals(1001L, RequestIdentityHolder.getUserId().orElseThrow());
    }

    @Test
    void shouldRejectMissingUserHeader() {
        authProperties.setEnabled(true);
        MockHttpServletRequest request = request("/api/settlement/calculate");

        assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void shouldRejectInvalidUserHeader() {
        authProperties.setEnabled(true);
        MockHttpServletRequest request = request("/api/user/coupons");
        request.addHeader("X-User-Id", "abc");

        assertThrows(BusinessException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void shouldAuthenticateAdminByToken() {
        authProperties.setEnabled(true);
        authProperties.setAdminToken("secret-token");
        MockHttpServletRequest request = request("/api/admin/coupon-templates");
        request.addHeader("X-Admin-Token", "secret-token");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        assertTrue(RequestIdentityHolder.get().orElseThrow().admin());
    }

    @Test
    void shouldRejectInvalidAdminToken() {
        authProperties.setEnabled(true);
        authProperties.setAdminToken("secret-token");
        MockHttpServletRequest request = request("/api/admin/coupon-templates");
        request.addHeader("X-Admin-Token", "wrong-token");

        assertThrows(ForbiddenException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void shouldClearIdentityAfterCompletion() {
        authProperties.setEnabled(true);
        MockHttpServletRequest request = request("/api/user/coupons");
        request.addHeader("X-User-Id", "1001");
        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);

        assertTrue(RequestIdentityHolder.get().isEmpty());
    }

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }
}
