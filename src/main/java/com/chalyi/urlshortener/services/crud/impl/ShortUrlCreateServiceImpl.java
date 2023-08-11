package com.chalyi.urlshortener.services.crud.impl;

import com.chalyi.urlshortener.config.ShortUrlConfiguration;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.RedisUrlKeys;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.id.ShortIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.UnifiedJedis;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlCreateServiceImpl implements ShortUrlCreateService {

    private final UnifiedJedis unifiedJedis;
    private final ShortIdGenerator shortIdGenerator;
    private final ShortUrlConfiguration configuration;
    private final RedisUrlKeys redisUrlKeys;
    private final Clock clock;

    /**
     * This implementation manually sets all required keys in redis, sets expirations when required.
     * Uniqueness of generated tokens is not validated by this method.
     * Technically a collision for delete token is not an issue, since it has to be pseudo-random and we should have
     * unique pair of short-url and delete-token
     *
     * @param request with parameters to create a short url for given original url
     * @return a response with short-url and delete-token.
     */
    @Override
    public CreateShortUrlResponse create(CreateShortUrlRequest request) {
        String shortUrl = shortIdGenerator.generate(configuration.getUrlLength());
        String deleteToken = shortIdGenerator.generate(configuration.getRemoveTokenLength());
        CreateShortUrlResponse response = new CreateShortUrlResponse(shortUrl, deleteToken);
        String key = redisUrlKeys.urlHashKey(shortUrl);
        try (Pipeline pipeline = (Pipeline) unifiedJedis.pipelined()) {

            //add hash with all information about short url
            pipeline.hset(key, Map.of(
                    "originalUrl", request.getOriginalUrl(),
                    "shortUrl", shortUrl,
                    "deleteToken", deleteToken,
                    "visitors", "0",
                    "created", LocalDateTime.now(clock).format(DateTimeFormatter.ISO_DATE_TIME),
                    "expire", getExpireDate(request)
            ));

            if (request.getUserAgent() != null && !request.getUserAgent().isEmpty()) {
                pipeline.sadd(redisUrlKeys.urlUserAgentsSet(shortUrl), request.getUserAgent());
                pipeline.zincrby(redisUrlKeys.mostUsedUserAgentsKey(), 1.0, request.getUserAgent());
            }

            long nowTimeStamp = Instant.now(clock).toEpochMilli();

            //add new entity to most viewed urls
            pipeline.zadd(redisUrlKeys.mostViewedUrlSortedSetKey(), 0.0, shortUrl);

            //increment total number of urls
            pipeline.incr(redisUrlKeys.getTotalCountKey());

            pipeline.tsAdd(redisUrlKeys.urlCreatedTimeSeriesKey(), nowTimeStamp, 1);
            pipeline.tsCreate(redisUrlKeys.urlVisitedTimeSeriesKey(shortUrl));

            if (request.getExpire() != 0) {
                expireKeys(pipeline, shortUrl, request.getExpire());
            }

            log.trace("Created short url = {} for original url = {}", shortUrl, request.getOriginalUrl());
            return response;
        }
    }

    /**
     * Set expiration dates for all keys related to given short url
     *
     * @param pipeline - the jedis pipeline where all requests are executed
     * @param shortUrl - given short url
     * @param expire   - expiration in seconds
     */
    private void expireKeys(Pipeline pipeline, String shortUrl, long expire) {
        pipeline.expire(redisUrlKeys.urlHashKey(shortUrl), expire);
        pipeline.expire(redisUrlKeys.getUniqueVisitorsKey(shortUrl), expire);
        pipeline.expire(redisUrlKeys.urlUserAgentsSet(shortUrl), expire);
        pipeline.expire(redisUrlKeys.urlLastVisitedKey(shortUrl), expire);
        pipeline.expire(redisUrlKeys.urlVisitedTimeSeriesKey(shortUrl), expire);
    }

    /**
     * The expiration date will be stored as a part of a short's url hash.
     * The format of the expiration date is DateTimeFormatter.ISO_DATE_TIME
     *
     * @param request - create request with expiration date
     * @return LocalDateTime.MAX is expire is 0 (no expiration date),
     * otherwise LocalDateTime.now() + expire seconds from the request
     */
    private String getExpireDate(CreateShortUrlRequest request) {
        return request.getExpire() != 0
                ? LocalDateTime.now(clock).plusSeconds(request.getExpire()).format(DateTimeFormatter.ISO_DATE_TIME)
                : LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
