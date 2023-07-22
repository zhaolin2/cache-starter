package com.sinozo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;

import javax.annotation.Resource;

@Configuration
@ConditionalOnBean(RedisCacheManager.class)
public class CacheConfig extends CachingConfigurerSupport {
    @Resource
    private RedisCacheManager redisCacheManager;

    /**
     * 重写这个方法，目的是用以提供默认的cacheManager
     *
     * @return
     * @author Stephen.Shi
     */
    @Override
    public CacheManager cacheManager() {
        return redisCacheManager;
    }

    /**
     * 如果cache出错， 我们会记录在日志里，方便排查，比如反序列化异常
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }


}