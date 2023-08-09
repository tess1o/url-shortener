package com.chalyi.urlshortener.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

@Configuration
@ConfigurationProperties(prefix = "redis")
@Setter
public class JedisConfiguration {

    private String host;
    private int port;

    @Bean
    UnifiedJedis unifiedJedis() {
        return new UnifiedJedis(new HostAndPort(host, port));
    }
}
