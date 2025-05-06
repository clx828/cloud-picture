package com.caden.picturebackend.aop;

import com.caden.picturebackend.annotation.CacheEvictCustom;
import com.caden.picturebackend.manager.CacheManage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Aspect
@Component
@Slf4j
public class CacheEvictAspect {
    @Resource
    private CacheManage cacheManage;

    @Around("@annotation(cacheEvictCustom)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, CacheEvictCustom cacheEvictCustom) throws Throwable {
        String keyPrefix = cacheEvictCustom.keyPrefix();
        String keySuffix = cacheEvictCustom.keySuffix();

        boolean isPattern = "*".equals(keySuffix);
        String fullKey = isPattern ? keyPrefix : keyPrefix + keySuffix;

        log.info("清除缓存: {}", fullKey);
        cacheManage.removeCache(fullKey, isPattern);

        return joinPoint.proceed();
    }
}
