package com.xinjia.coupon.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI xinCouponOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("XinCoupon API")
                        .description("优惠券系统核心接口文档")
                        .version("0.0.1"));
    }
}
