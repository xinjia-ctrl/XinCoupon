package com.xinjia.coupon.common.mq;

public record MqConsumeMark(
        boolean accepted,
    MqConsumeState currentState
) {

    public static MqConsumeMark acquired() {
        return new MqConsumeMark(true, MqConsumeState.CONSUMING);
    }

    public static MqConsumeMark duplicate(MqConsumeState currentState) {
        return new MqConsumeMark(false, currentState);
    }
}
