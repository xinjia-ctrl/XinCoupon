package com.xinjia.coupon.common.mq;

import java.time.Duration;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class NoMQDuplicateConsumeAspect {

    private static final Logger log = LoggerFactory.getLogger(NoMQDuplicateConsumeAspect.class);

    private final MqConsumeIdempotentStore mqConsumeIdempotentStore;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public NoMQDuplicateConsumeAspect(MqConsumeIdempotentStore mqConsumeIdempotentStore) {
        this.mqConsumeIdempotentStore = mqConsumeIdempotentStore;
    }

    @Around("@annotation(noMQDuplicateConsume)")
    public Object around(ProceedingJoinPoint joinPoint, NoMQDuplicateConsume noMQDuplicateConsume) throws Throwable {
        String key = resolveKey(joinPoint, noMQDuplicateConsume.key());
        MqConsumeMark mark = mqConsumeIdempotentStore.markConsumingIfAbsent(
                key,
                Duration.ofSeconds(noMQDuplicateConsume.consumingTtlSeconds())
        );
        if (!mark.accepted()) {
            log.info("跳过重复 MQ 消息, key={}, state={}", key, mark.currentState());
            return null;
        }
        try {
            Object result = joinPoint.proceed();
            mqConsumeIdempotentStore.markConsumed(
                    key,
                    Duration.ofSeconds(noMQDuplicateConsume.consumedTtlSeconds())
            );
            return result;
        } catch (Throwable throwable) {
            mqConsumeIdempotentStore.markRetryable(key);
            throw throwable;
        }
    }

    private String resolveKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        EvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
            if (parameterNames != null && i < parameterNames.length) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        Object value = expressionParser.parseExpression(keyExpression).getValue(context);
        return String.valueOf(value);
    }
}
