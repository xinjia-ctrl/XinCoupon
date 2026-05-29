package com.xinjia.coupon.user.coupon.infrastructure;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Repository;

import com.xinjia.coupon.user.coupon.domain.UserCoupon;

@Repository
public class InMemoryReceiveRequestRepository implements ReceiveRequestRepository {

    private final ConcurrentMap<String, UserCoupon> receivedResults = new ConcurrentHashMap<>();

    @Override
    public Optional<UserCoupon> findResult(String requestId) {
        return Optional.ofNullable(receivedResults.get(requestId));
    }

    @Override
    public void saveResult(String requestId, UserCoupon userCoupon) {
        receivedResults.putIfAbsent(requestId, userCoupon);
    }
}
