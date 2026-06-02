package com.xinjia.coupon.dispatch.event.infrastructure.rocketmq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xincoupon.dispatch.rocketmq")
public class RocketMqDispatchProperties {

    private String topic = "xin-coupon-event";
    private String receivedTag = "coupon-received";
    private String consumerGroup = "xin-coupon-event-consumer";

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getReceivedTag() {
        return receivedTag;
    }

    public void setReceivedTag(String receivedTag) {
        this.receivedTag = receivedTag;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String receivedDestination() {
        return topic + ":" + receivedTag;
    }
}
