package com.caden.picturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvictCustom {
    String keyPrefix (); // 缓存的名称（如 Redis 的 key 前缀）
    String keySuffix ();       // 缓存的 key（key后缀）
}