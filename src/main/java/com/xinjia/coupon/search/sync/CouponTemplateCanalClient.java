package com.xinjia.coupon.search.sync;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "xincoupon.search.canal.enabled", havingValue = "true")
public class CouponTemplateCanalClient implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(CouponTemplateCanalClient.class);
    private static final String COUPON_TEMPLATE_TABLE = "coupon_template";

    private final SearchSyncProperties searchSyncProperties;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CanalCouponTemplateDocumentMapper documentMapper;
    private volatile boolean running;
    private Thread worker;
    private CanalConnector connector;

    public CouponTemplateCanalClient(
            SearchSyncProperties searchSyncProperties,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.searchSyncProperties = searchSyncProperties;
        this.applicationEventPublisher = applicationEventPublisher;
        this.documentMapper = new CanalCouponTemplateDocumentMapper();
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        SearchSyncProperties.Canal canal = searchSyncProperties.getCanal();
        connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(canal.getHost(), canal.getPort()),
                canal.getDestination(),
                canal.getUsername(),
                canal.getPassword()
        );
        running = true;
        worker = new Thread(this::consumeLoop, "coupon-template-canal-client");
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
        if (connector != null) {
            connector.disconnect();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void consumeLoop() {
        SearchSyncProperties.Canal canal = searchSyncProperties.getCanal();
        try {
            connector.connect();
            connector.subscribe(canal.getFilter());
            connector.rollback();
            while (running) {
                Message message = connector.getWithoutAck(canal.getBatchSize());
                long batchId = message.getId();
                if (batchId == -1 || message.getEntries().isEmpty()) {
                    sleep(canal.getEmptySleepMillis());
                    continue;
                }
                try {
                    handleEntries(message);
                    connector.ack(batchId);
                } catch (RuntimeException exception) {
                    connector.rollback(batchId);
                    log.warn("处理 Canal binlog 失败, batchId={}", batchId, exception);
                    sleep(canal.getEmptySleepMillis());
                }
            }
        } finally {
            connector.disconnect();
        }
    }

    private void handleEntries(Message message) {
        for (CanalEntry.Entry entry : message.getEntries()) {
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA
                    || !COUPON_TEMPLATE_TABLE.equals(entry.getHeader().getTableName())) {
                continue;
            }
            CanalEntry.RowChange rowChange = parseRowChange(entry);
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                publish(rowChange.getEventType(), rowData);
            }
        }
    }

    private CanalEntry.RowChange parseRowChange(CanalEntry.Entry entry) {
        try {
            return CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        } catch (InvalidProtocolBufferException exception) {
            throw new IllegalStateException("解析 Canal RowChange 失败", exception);
        }
    }

    private void publish(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        if (eventType == CanalEntry.EventType.DELETE) {
            Map<String, String> before = toMap(rowData.getBeforeColumnsList());
            applicationEventPublisher.publishEvent(CouponTemplateBinlogEvent.delete(Long.valueOf(before.get("id"))));
            return;
        }
        Map<String, String> after = toMap(rowData.getAfterColumnsList());
        applicationEventPublisher.publishEvent(CouponTemplateBinlogEvent.upsert(documentMapper.fromRow(after)));
    }

    private Map<String, String> toMap(java.util.List<CanalEntry.Column> columns) {
        Map<String, String> values = new HashMap<>();
        for (CanalEntry.Column column : columns) {
            values.put(column.getName(), column.getValue());
        }
        return values;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
