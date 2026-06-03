package com.xinjia.coupon.common.sharding;

public record ShardTarget(String logicalTable, String actualTable, int shardIndex) {
}
