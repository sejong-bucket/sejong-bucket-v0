package com.ime.lockmanager.common.aop;

import com.ime.lockmanager.common.aop.meta.DistributeLock;
import com.ime.lockmanager.common.aop.meta.ReserveLock;
import com.ime.lockmanager.common.format.exception.locker.AlreadyReservedLockerException;
import com.ime.lockmanager.common.util.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

@Order(2)
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ReserveAop {
    private final AopForTransaction aopForTransaction;
    private final RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(com.ime.lockmanager.common.aop.meta.ReserveLock)")
    public Object customLock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ReserveLock distributeLock = method.getAnnotation(ReserveLock.class);
        String key = "" + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributeLock.key(), distributeLock.identifier());
        validateIsPossibleToReserve(key);
        Object proceed = aopForTransaction.proceed(joinPoint);
        validateAndRegister(key);
        return proceed;
    }

    private void validateIsPossibleToReserve(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            throw new AlreadyReservedLockerException();
        }

    }

    private void validateAndRegister(String key) {
        validateIsPossibleToReserve(key);
        redisTemplate.opsForValue().set(key, String.valueOf(true), Duration.ofHours(2));
    }
}
