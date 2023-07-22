package com.sinozo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

import java.time.Duration;
import java.util.Optional;

import static org.springframework.data.redis.cache.RedisCacheConfiguration.registerDefaultConverters;

@Configuration
public class CachingConfiguration {

    @Bean
    @ConditionalOnBean(LettuceConnectionFactory.class)
    @ConditionalOnMissingBean(RedisCacheManager.class)
    public RedisCacheManager redisCache(LettuceConnectionFactory lettuceConnectionFactory, RedisCacheConfiguration cacheConfiguration) {
        return RedisCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(cacheConfiguration)
//                .enableStatistics()
                .build();
    }

    @Bean
    @ConditionalOnBean(CacheProperties.class)
    @ConditionalOnMissingBean(RedisCacheConfiguration.class)
    public RedisCacheConfiguration cacheConfiguration(CacheProperties cacheProperties) {
        final CacheProperties.Redis redisProperties = cacheProperties.getRedis();

        final Duration durationTtl = Optional.ofNullable(redisProperties.getTimeToLive()).orElse(Duration.ZERO);
        final Boolean isCacheNullValues = Optional.of(redisProperties.isCacheNullValues()).orElse(true);
        final String keyPrefix = Optional.ofNullable(redisProperties.getKeyPrefix()).orElse("application");
        RedisSerializationContext.SerializationPair<String> keySerializationPair= RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string());
        RedisSerializationContext.SerializationPair<?> valueSerializationPair= RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(new ObjectMapper()));
        ConversionService conversionService= getDefaultConversionService();

        final CacheKeyPrefix cacheKeyPrefix = s -> {
            return s;
        };

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(durationTtl)
                .computePrefixWith(cacheKeyPrefix)
                .withConversionService(conversionService)
                .serializeKeysWith(keySerializationPair)
                .serializeValuesWith(valueSerializationPair);

        if (isCacheNullValues){
            cacheConfiguration = cacheConfiguration.disableCachingNullValues();
        }

        return cacheConfiguration;
    }

    /**
     * {@link RedisCacheConfiguration#defaultCacheConfig(java.lang.ClassLoader)}
     */
    private ConversionService getDefaultConversionService(){
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        registerDefaultConverters(conversionService);

        return conversionService;
    }

//    @Bean
//    protected LettuceConnectionFactory redisConnectionFactory() {
//        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
//                .master(redisProperties.getSentinel().getMaster());
//        redisProperties.getSentinel().getNodes().forEach(s -> sentinelConfig.sentinel(s, redisProperties.getPort()));
//        sentinelConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
//
//        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//                .commandTimeout(redisCommandTimeout).readFrom(ReadFrom.REPLICA_PREFERRED).build();
//        return new LettuceConnectionFactory(sentinelConfig, clientConfig);

//        return new LettuceConnectionFactory();
//    }

    /**
     * {@link Caffeine}
     *
     * @param cacheProperties
     * @return
     */
    @Bean
    public CacheManager localCache(CacheProperties cacheProperties) {
        final CacheProperties.Caffeine caffeineProperties = cacheProperties.getCaffeine();

        final String spec = caffeineProperties.getSpec();
        final CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

        caffeineCacheManager.setCacheSpecification(spec);
        caffeineCacheManager.setAllowNullValues(true);

        return caffeineCacheManager;
    }

    RedissonClient getClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        config.setThreads(4);

        return Redisson.create(config);
    }
}
