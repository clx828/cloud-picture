package com.caden.picturebackend.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheManage {
    private final Cache<String, String> caffeineCache;
    private final StringRedisTemplate redisTemplate;
    private final Random random = new Random();

    // 默认最小过期时间 (5分钟 = 300,000毫秒)
    private static final int DEFAULT_MIN_EXPIRY = 5 * 60 * 1000;
    // 默认最大过期时间 (10分钟 = 600,000毫秒)
    private static final int DEFAULT_MAX_EXPIRY = 10 * 60 * 1000;

    @Autowired
    public CacheManage(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.caffeineCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    /**
     * 获取缓存，先从一级缓存Caffeine获取，如果没有则从二级缓存Redis获取
     * @param key 缓存键
     * @return 缓存值，如果不存在则返回空Optional
     */
    public String getCache(String key) {
        // 先从一级缓存Caffeine获取
        String value = caffeineCache.getIfPresent(key);

        // 如果一级缓存中没有，则从二级缓存Redis获取
        if (value == null) {
            value = redisTemplate.opsForValue().get(key);

            // 如果在Redis中找到，则同时更新到Caffeine
            if (value != null) {
                caffeineCache.put(key, value);
            }
        }

        return value; // 直接返回字符串，如果没有找到则返回 null
    }

    /**
     * 设置缓存，同时更新一级和二级缓存，并设置随机过期时间
     * @param key 缓存键
     * @param value 缓存值
     * @param minExpiry 最小过期时间(毫秒)
     * @param maxExpiry 最大过期时间(毫秒)
     */
    public void setCache(String key, String value, int minExpiry, int maxExpiry) {
        // 参数校验
        if (minExpiry < 0 || maxExpiry < minExpiry) {
            throw new IllegalArgumentException("无效的过期时间范围");
        }
        // 计算随机过期时间（毫秒）
        int randomExpiry = minExpiry + random.nextInt(maxExpiry - minExpiry + 1);

        // 更新一级缓存Caffeine (Caffeine不支持对单个元素设置过期时间，这里使用全局设置)
        caffeineCache.put(key, value);

        // 更新二级缓存Redis，并设置随机过期时间
        redisTemplate.opsForValue().set(key, value, randomExpiry, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存，使用默认的5-10分钟随机过期时间
     * @param key 缓存键
     * @param value 缓存值
     */
    public void setCache(String key, String value) {
        setCache(key, value, DEFAULT_MIN_EXPIRY, DEFAULT_MAX_EXPIRY);
    }

    /**
     * 删除缓存，同时从一级和二级缓存中删除
     * @param key 缓存键
     */
    public void removeCache(String key) {
        // 从一级缓存Caffeine中删除
        caffeineCache.invalidate(key);

        // 从二级缓存Redis中删除
        redisTemplate.delete(key);
    }
    public void removeCache(String key, boolean isPattern) {
        if (!isPattern) {
            caffeineCache.invalidate(key);
            redisTemplate.delete(key);
            return;
        }

        // 1. 删除 Caffeine 中以 key 开头的所有缓存
        caffeineCache.asMap().keySet().removeIf(k -> k.startsWith(key));

        // 2. 删除 Redis 中以 key 开头的所有缓存（使用 scan 比 keys 安全）
        Set<String> keys = redisTemplate.keys(key + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        // 清空一级缓存Caffeine
        caffeineCache.invalidateAll();

        // 注意：清空所有Redis缓存是危险操作，这里只实现接口，但建议实际使用时谨慎
        // 可以实现为仅清除特定前缀的缓存，或使用专用的Redis库
    }

    /**
     * 检查缓存键是否存在
     * @param key 缓存键
     * @return 是否存在于任一级缓存
     */
    public boolean exists(String key) {
        // 检查一级缓存
        if (caffeineCache.getIfPresent(key) != null) {
            return true;
        }

        // 检查二级缓存
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}