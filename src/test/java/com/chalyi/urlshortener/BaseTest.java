package com.chalyi.urlshortener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.UnifiedJedis;

import java.nio.charset.StandardCharsets;

public abstract class BaseTest {

    public static final int REDIS_PORT = 6379;
    public static final GenericContainer<?> redis;

    @Autowired
    private UnifiedJedis unifiedJedis;

    protected final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        redis =
                new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server"))
                        .withExposedPorts(REDIS_PORT)
                        .withReuse(false);
        redis.start();
        System.setProperty("redis.host", redis.getHost());
        System.setProperty("redis.port", redis.getMappedPort(REDIS_PORT).toString());

        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    protected void flushAll() {
        unifiedJedis.sendBlockingCommand(() -> "flushall".getBytes(StandardCharsets.UTF_8), new String[]{});
    }
}
