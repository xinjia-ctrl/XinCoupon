package com.xinjia.coupon.search.sync;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xincoupon.search")
public class SearchSyncProperties {

    private SyncMode syncMode = SyncMode.APPLICATION_EVENT;
    private IndexType indexType = IndexType.MEMORY;
    private Elasticsearch elasticsearch = new Elasticsearch();
    private Canal canal = new Canal();

    public SyncMode getSyncMode() {
        return syncMode;
    }

    public void setSyncMode(SyncMode syncMode) {
        this.syncMode = syncMode;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }

    public Elasticsearch getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(Elasticsearch elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public enum SyncMode {
        APPLICATION_EVENT,
        OUTBOX,
        CANAL
    }

    public enum IndexType {
        MEMORY,
        ELASTICSEARCH
    }

    public static class Elasticsearch {

        private String url = "http://localhost:9200";
        private String indexName = "coupon_template";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIndexName() {
            return indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }
    }

    public static class Canal {

        private boolean enabled = false;
        private String host = "localhost";
        private int port = 11111;
        private String destination = "example";
        private String username = "";
        private String password = "";
        private String filter = "xin_coupon.coupon_template";
        private int batchSize = 100;
        private long emptySleepMillis = 1000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getEmptySleepMillis() {
            return emptySleepMillis;
        }

        public void setEmptySleepMillis(long emptySleepMillis) {
            this.emptySleepMillis = emptySleepMillis;
        }
    }
}
