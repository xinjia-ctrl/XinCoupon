package com.xinjia.coupon.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTests {

    @Test
    void successShouldWrapData() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertThat(response.code()).isZero();
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("ok");
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void failureShouldKeepCodeAndMessage() {
        ApiResponse<Void> response = ApiResponse.failure(40001, "请求参数不合法");

        assertThat(response.code()).isEqualTo(40001);
        assertThat(response.message()).isEqualTo("请求参数不合法");
        assertThat(response.data()).isNull();
        assertThat(response.timestamp()).isNotNull();
    }
}
