package com.xinjia.coupon.settlement.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xinjia.coupon.common.api.ApiResponse;
import com.xinjia.coupon.settlement.application.SettlementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping("/calculate")
    public ApiResponse<SettlementCalculateView> calculate(@Valid @RequestBody SettlementCalculateRequest request) {
        return ApiResponse.success(settlementService.calculate(request));
    }
}
