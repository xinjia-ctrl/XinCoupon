package com.xinjia.coupon.common.idempotent;

import java.time.Duration;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.xinjia.coupon.common.enums.ErrorCode;
import com.xinjia.coupon.common.exception.BusinessException;

@Aspect
@Component
public class NoDuplicateSubmitAspect {

    private final DuplicateSubmitStore duplicateSubmitStore;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public NoDuplicateSubmitAspect(DuplicateSubmitStore duplicateSubmitStore) {
        this.duplicateSubmitStore = duplicateSubmitStore;
    }

    @Around("@annotation(noDuplicateSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, NoDuplicateSubmit noDuplicateSubmit) throws Throwable {
        String key = resolveKey(joinPoint, noDuplicateSubmit.key());
        boolean marked = duplicateSubmitStore.markIfAbsent(key, Duration.ofSeconds(noDuplicateSubmit.ttlSeconds()));
        if (!marked) {
            throw new BusinessException(ErrorCode.BUSINESS_REJECTED, noDuplicateSubmit.message());
        }
        return joinPoint.proceed();
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
