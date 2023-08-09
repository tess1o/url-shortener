package com.chalyi.urlshortener.services.crud.impl;

import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;
import com.chalyi.urlshortener.services.RedisUrlKeys;
import com.chalyi.urlshortener.services.crud.ShortUrlVisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.UnifiedJedis;

import java.time.Clock;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class ShortUrlVisitServiceImpl implements ShortUrlVisitService {

    private final UnifiedJedis unifiedJedis;
    private final RedisUrlKeys redisUrlKeys;
    private final Clock clock;

    @Override
    public String visitShortUrl(VisitShortUrlRequest request) {
        String urlHashKey = redisUrlKeys.urlHashKey(request.getShortUrl());
        if (!unifiedJedis.exists(urlHashKey)) {
            throw new NoSuchUrlFound(request.getShortUrl());
        }
        Response<String> originalUrl;
        try (Pipeline pipeline = (Pipeline) unifiedJedis.pipelined()) {
            originalUrl = pipeline.hget(urlHashKey, "originalUrl");
            if (request.getUserAgent() != null && !request.getUserAgent().isEmpty()) {
                pipeline.sadd(redisUrlKeys.urlUserAgentsSet(request.getShortUrl()), request.getUserAgent());
                pipeline.zincrby(redisUrlKeys.mostUsedUserAgentsKey(), 1, request.getUserAgent());
            }
            long nowTimeStamp = Instant.now(clock).toEpochMilli();
            pipeline.hincrBy(urlHashKey, "visitors", 1);
            pipeline.incr(redisUrlKeys.getTotalVisitedCountKey());
            pipeline.zincrby(redisUrlKeys.mostViewedUrlSortedSetKey(), 1, request.getShortUrl());
            pipeline.pfadd(redisUrlKeys.getUniqueVisitorsKey(request.getShortUrl()), request.getIpAddress().toString());
            pipeline.tsAdd(redisUrlKeys.urlVisitedTimeSeriesKey(request.getShortUrl()), nowTimeStamp, 1);
            pipeline.tsAdd(redisUrlKeys.urlVisitedTimeSeriesKey(), nowTimeStamp, 1);
            pipeline.rpush(redisUrlKeys.urlLastVisitedKey(request.getShortUrl()), Long.toString(nowTimeStamp));
            pipeline.ltrim(redisUrlKeys.urlLastVisitedKey(request.getShortUrl()), -10, -1);
        }
        return originalUrl.get();
    }
}
