package com.xinjia.coupon.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GatewayRequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayRequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        return chain.filter(exchange)
                .doFinally(signalType -> LOGGER.info(
                        "gateway request completed, method={}, path={}, status={}, cost={}ms",
                        exchange.getRequest().getMethod(),
                        exchange.getRequest().getURI().getPath(),
                        exchange.getResponse().getStatusCode(),
                        System.currentTimeMillis() - start
                ));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
