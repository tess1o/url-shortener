package com.chalyi.urlshortener.services.crud.impl;

import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.services.RedisUrlKeys;
import com.chalyi.urlshortener.services.crud.ShortUrlInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.UnifiedJedis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlInfoServiceImpl implements ShortUrlInfoService {

    private final UnifiedJedis unifiedJedis;
    private final RedisUrlKeys redisUrlKeys;

    @Override
    public ShortUrl info(String shortUrl) {
        String urlHashKey = redisUrlKeys.urlHashKey(shortUrl);
        if (!unifiedJedis.exists(urlHashKey)) {
            throw new NoSuchUrlFound(shortUrl);
        }
        Response<Map<String, String>> urlHashResponse;
        Response<Set<String>> userAgentsResponse;
        Response<Long> uniqueVisitorsResponse;

        try (Pipeline pipeline = (Pipeline) unifiedJedis.pipelined()) {
            urlHashResponse = pipeline.hgetAll(urlHashKey);
            userAgentsResponse = pipeline.smembers(redisUrlKeys.urlUserAgentsSet(shortUrl));
            uniqueVisitorsResponse = pipeline.pfcount(redisUrlKeys.getUniqueVisitorsKey(shortUrl));
        }

        Map<String, String> urlHash = urlHashResponse.get();
        Set<String> userAgents = userAgentsResponse.get();
        long uniqueVisitors = uniqueVisitorsResponse.get();

        return ShortUrl.builder()
                .userAgents(userAgents)
                .originalUrl(urlHash.get("originalUrl"))
                .shortUrl(shortUrl)
                .visitors(Long.parseLong(urlHash.get("visitors")))
                .created(LocalDateTime.parse(urlHash.get("created"), DateTimeFormatter.ISO_DATE_TIME))
                .expire(LocalDateTime.parse(urlHash.get("expire"), DateTimeFormatter.ISO_DATE_TIME))
                .uniqueVisitors(uniqueVisitors)
                .build();
    }
}
