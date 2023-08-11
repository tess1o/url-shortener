package com.chalyi.urlshortener.services.crud.impl;

import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.exceptions.WrongDeleteTokenException;
import com.chalyi.urlshortener.services.RedisUrlKeys;
import com.chalyi.urlshortener.services.crud.ShortUrlDeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.UnifiedJedis;

@RequiredArgsConstructor
@Service
public class ShortUrlDeleteServiceImpl implements ShortUrlDeleteService {

    private final UnifiedJedis unifiedJedis;
    private final RedisUrlKeys redisUrlKeys;

    /**
     * This method deletes the short url from the redis. It also performs clean up for the keys used by
     * the short url
     *
     * @param shortUrl    - short url to be deleted
     * @param deleteToken - delete token
     */
    @Override
    public void delete(String shortUrl, String deleteToken) {
        String urlHashKey = redisUrlKeys.urlHashKey(shortUrl);
        if (!unifiedJedis.exists(urlHashKey)) {
            throw new NoSuchUrlFound(shortUrl);
        }
        String tokenFromHash = unifiedJedis.hget(urlHashKey, "deleteToken");
        if (deleteToken.equals(tokenFromHash)) {
            try (Pipeline pipeline = (Pipeline) unifiedJedis.pipelined()) {
                pipeline.del(urlHashKey);
                pipeline.del(redisUrlKeys.urlLastVisitedKey(shortUrl));
                pipeline.del(redisUrlKeys.urlVisitedTimeSeriesKey(shortUrl));
                pipeline.del(redisUrlKeys.getUniqueVisitorsKey(shortUrl));
                pipeline.del(redisUrlKeys.urlUserAgentsSet(shortUrl));
            }
        } else {
            throw new WrongDeleteTokenException("deleteToken " + deleteToken +
                    " is wrong for shortUrl " + shortUrl);
        }
    }
}
