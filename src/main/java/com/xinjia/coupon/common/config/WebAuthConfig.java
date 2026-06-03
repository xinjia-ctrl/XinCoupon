package com.xinjia.coupon.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.xinjia.coupon.common.auth.AuthProperties;
import com.xinjia.coupon.common.auth.HeaderAuthInterceptor;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class WebAuthConfig implements WebMvcConfigurer {

    private final AuthProperties authProperties;

    public WebAuthConfig(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HeaderAuthInterceptor(authProperties))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/system/health");
    }
}
