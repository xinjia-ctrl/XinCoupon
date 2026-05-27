package com.xinjia.coupon.system;

import java.time.OffsetDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/system/health")
    public HealthView health() {
        return new HealthView("UP", "XinCoupon service is running", OffsetDateTime.now());
    }

    public record HealthView(String status, String message, OffsetDateTime checkedAt) {
    }
}
