package com.xinjia.coupon.search.sync;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xincoupon.search")
public class SearchSyncProperties {

    private SyncMode syncMode = SyncMode.APPLICATION_EVENT;

    public SyncMode getSyncMode() {
        return syncMode;
    }

    public void setSyncMode(SyncMode syncMode) {
        this.syncMode = syncMode;
    }

    public enum SyncMode {
        APPLICATION_EVENT,
        OUTBOX,
        CANAL
    }
}
