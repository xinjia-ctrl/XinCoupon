package com.xinjia.coupon.settlement.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.xinjia.coupon.settlement.web.SettlementCalculateRequest;
import com.xinjia.coupon.settlement.web.SettlementCalculateView;

@Service
public class SettlementService {

    public SettlementCalculateView calculate(SettlementCalculateRequest request) {
        return new SettlementCalculateView(
                request.userId(),
                request.orderNo(),
                request.merchantId(),
                request.orderAmount(),
                0L,
                request.orderAmount(),
                List.of(),
                null
        );
    }
}
