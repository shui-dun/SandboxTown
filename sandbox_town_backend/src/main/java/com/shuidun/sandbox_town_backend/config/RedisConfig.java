package com.shuidun.sandbox_town_backend.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * redis配置类
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
    /**
     * 配置Jackson2JsonRedisSerializer
     * Java默认的序列化方式序列化后的数据占用空间小，但是可读性差
     * 因此，使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
     * 这样，存储在redis中的数据就是json格式的，可读性好
     */
    private Jackson2JsonRedisSerializer<Object> createJackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().
                allowIfBaseType(Object.class).build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING);
        mapper.registerModule(new JavaTimeModule());
        // 添加不可变集合的支持，不然在序列化 List.of 等不可变集合时会报错
        mapper.registerModule(new Jdk8Module());
        serializer.setObjectMapper(mapper);
        return serializer;
    }

    /**
     * 自定义redisTemplate
     */
    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 这就是我们要返回的redisTemplate
        // 在任意地方，我们可以通过@Autowired注入这个redisTemplate来使用redis
        // 例如，通过redisTemplate.opsForValue().set("xxx", xxxBean);来存储一个对象
        // 又例如，通过redisTemplate.opsForValue().get("xxx");来获取这个对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 配置序列化器
        Jackson2JsonRedisSerializer<Object> serializer = createJackson2JsonRedisSerializer();
        template.setValueSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 自定义redisCacheManager，这样我们就可以使用@Cacheable等注解了
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 定义字符串序列化方式（用作key的序列化方式）
        RedisSerializer<String> strSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> objectJackson2JsonRedisSerializer = createJackson2JsonRedisSerializer();

        // 定制缓存序列化方式
        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        // 配置缓存过期时间（5分钟）
                        .entryTtl(Duration.ofMinutes(5))
                        // 配置key的序列化方式
                        .serializeKeysWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(strSerializer))
                        // 配置value的序列化方式
                        .serializeValuesWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(objectJackson2JsonRedisSerializer));
        // 使用自定义的缓存配置初始化一个RedisCacheManager
        return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(config).build();
    }
}
