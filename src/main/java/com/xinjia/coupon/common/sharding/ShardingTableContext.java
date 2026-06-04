package com.xinjia.coupon.common.sharding;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ShardingTableContext {

    private static final ThreadLocal<Map<String, String>> TABLE_NAMES = ThreadLocal.withInitial(HashMap::new);

    private ShardingTableContext() {
    }

    public static String resolve(String logicTableName) {
        return TABLE_NAMES.get().getOrDefault(logicTableName, logicTableName);
    }

    public static <T> T use(String logicTableName, String actualTableName, Supplier<T> supplier) {
        Map<String, String> tableNames = TABLE_NAMES.get();
        String previous = tableNames.put(logicTableName, actualTableName);
        try {
            return supplier.get();
        } finally {
            if (previous == null) {
                tableNames.remove(logicTableName);
            } else {
                tableNames.put(logicTableName, previous);
            }
            if (tableNames.isEmpty()) {
                TABLE_NAMES.remove();
            }
        }
    }
}
