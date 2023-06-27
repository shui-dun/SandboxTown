package com.shuidun.sandbox_town_backend.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // Java默认的序列化方式序列化后的数据占用空间小，但是可读性差
        // 因此，使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        // 这样，存储在redis中的数据就是json格式的，可读性好
        // 先配置一下Jackson2JsonRedisSerializer
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        // 设置key和value的序列化方式
        // key使用StringRedisSerializer
        // value使用Jackson2JsonRedisSerializer
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
        // 定义Jackson2JsonRedisSerializer（用作value的序列化方式）
        Jackson2JsonRedisSerializer objectJackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectJackson2JsonRedisSerializer.setObjectMapper(om);
        // 定制缓存序列化方式
        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        // 配置缓存过期时间（1分钟）
                        .entryTtl(Duration.ofMinutes(1))
                        // 配置key的序列化方式
                        .serializeKeysWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(strSerializer))
                        // 配置value的序列化方式
                        .serializeValuesWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(objectJackson2JsonRedisSerializer));
        // 使用自定义的缓存配置初始化一个RedisCacheManager
        RedisCacheManager cacheManager = RedisCacheManager
                .builder(redisConnectionFactory).cacheDefaults(config).build();
        return cacheManager;
    }
}