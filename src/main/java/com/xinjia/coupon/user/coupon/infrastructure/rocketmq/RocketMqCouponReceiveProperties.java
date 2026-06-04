package com.xinjia.coupon.user.coupon.infrastructure.rocketmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xincoupon.receive.rocketmq")
public class RocketMqCouponReceiveProperties {

    private String topic = "xin-coupon-receive";
    private String tag = "receive-requested";
    private String consumerGroup = "xin-coupon-receive-consumer";

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String destination() {
        return topic + ":" + tag;
    }
}
