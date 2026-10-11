package com.projectexpress.app.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

@Configuration
@Profile("!test")
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        JsonMapper jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("com.projectexpress.app.dto")
                                .allowIfSubType("java.util")
                                .build(),
                        DefaultTyping.NON_FINAL
                )
                .build();

        GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer(jsonMapper);

        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(jsonSerializer)
                        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}