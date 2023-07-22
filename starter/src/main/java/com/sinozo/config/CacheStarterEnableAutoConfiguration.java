package com.sinozo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.format.support.DefaultFormattingConversionService;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Optional;

import static org.springframework.data.redis.cache.RedisCacheConfiguration.registerDefaultConverters;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheStarterEnableAutoConfiguration {


    @Resource
    CacheProperties cacheProperties;

    @Bean
    @ConditionalOnBean({LettuceConnectionFactory.class,RedisCacheConfiguration.class})
    public RedisCacheManager redisCacheManager(LettuceConnectionFactory lettuceConnectionFactory, RedisCacheConfiguration cacheConfiguration) {
        return RedisCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(cacheConfiguration)
//                .enableStatistics()
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(RedisCacheConfiguration.class)
    public RedisCacheConfiguration cacheConfiguration() {
        final CacheProperties.Redis redisProperties = cacheProperties.getRedis();

        final Duration durationTtl = Optional.ofNullable(redisProperties.getTimeToLive()).orElse(Duration.ZERO);
        final Boolean isCacheNullValues = Optional.of(redisProperties.isCacheNullValues()).orElse(true);
        final String keyPrefix = Optional.ofNullable(redisProperties.getKeyPrefix()).orElse("application");
        RedisSerializationContext.SerializationPair<String> keySerializationPair= RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string());
        RedisSerializationContext.SerializationPair<?> valueSerializationPair= RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());
        ConversionService conversionService= getDefaultConversionService();



        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(durationTtl)
                .computePrefixWith(buildPrefix(keyPrefix))
                .withConversionService(conversionService)
                .serializeKeysWith(keySerializationPair)
                .serializeValuesWith(valueSerializationPair);

        if (!isCacheNullValues){
            cacheConfiguration = cacheConfiguration.disableCachingNullValues();
        }

        return cacheConfiguration;
    }

    //重新构建key的连接格式
    private CacheKeyPrefix buildPrefix(String keyPrefix){
        return (name) -> keyPrefix + ":" + name + ":";
    }

    /**
     * {@link RedisCacheConfiguration#defaultCacheConfig(ClassLoader)}
     */
    private ConversionService getDefaultConversionService(){
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        registerDefaultConverters(conversionService);

        return conversionService;
    }


    /**
     * {@link Caffeine}
     *
     * @param cacheProperties
     * @return
     */
    @Bean
    @ConditionalOnBean(CacheProperties.class)
    public CacheManager localCacheManager(CacheProperties cacheProperties) {
        final CacheProperties.Caffeine caffeineProperties = cacheProperties.getCaffeine();

        final String spec = caffeineProperties.getSpec();
        final CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

        caffeineCacheManager.setCacheSpecification(spec);
        caffeineCacheManager.setAllowNullValues(true);

        return caffeineCacheManager;
    }
}
