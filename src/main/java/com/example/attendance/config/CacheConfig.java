package com.example.attendance.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置
 *
 * 原理：
 *   Spring Cache 是一个抽象层——你只用 @Cacheable / @CacheEvict 注解，
 *   底层用什么缓存可以随时换，代码不用改。
 *
 *   当前：ConcurrentHashMap（JVM 内存缓存，不依赖外部服务）
 *   升级：加 Redis 依赖 → 改配置 → 底层自动切到 Redis
 *
 *   @Cacheable   = 查缓存 → 有就返回 / 没有就执行方法 → 结果存缓存
 *   @CacheEvict  = 清掉缓存，强制下次重新查库
 *
 * 面试话术：
 * "我用 Spring Cache 抽象层做缓存，@Cacheable 自动管理缓存的读写。
 * 当前用内存缓存，后续换成 Redis 只需改配置，业务代码零改动。"
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // ConcurrentMapCacheManager：基于 ConcurrentHashMap 的简单缓存
        // 如果将来装 Redis，这里换成 RedisCacheManager 即可
        return new ConcurrentMapCacheManager("courses");
    }
}
