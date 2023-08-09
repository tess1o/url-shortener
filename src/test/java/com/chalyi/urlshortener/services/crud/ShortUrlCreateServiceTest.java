package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.TestDirtyContext;
import com.chalyi.urlshortener.TimeMachine;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.RedisUrlKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import redis.clients.jedis.UnifiedJedis;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@TestDirtyContext
@Slf4j
public class ShortUrlCreateServiceTest extends BaseTest {

    private final static LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2023, 7, 12, 14, 0);

    private final ShortUrlCreateService createService;
    private final UnifiedJedis unifiedJedis;
    private final RedisUrlKeys redisUrlKeys;

    @MockBean
    private Clock clock;

    @Autowired
    public ShortUrlCreateServiceTest(ShortUrlCreateService createService, UnifiedJedis unifiedJedis, RedisUrlKeys redisUrlKeys) {
        this.createService = createService;
        this.unifiedJedis = unifiedJedis;
        this.redisUrlKeys = redisUrlKeys;
    }

    @BeforeEach
    public void cleanup() {
        flushAll();
    }

    @BeforeEach
    public void initClock() {
        TimeMachine timeMachine = new TimeMachine(clock);
        timeMachine.setFixedClock(LOCAL_DATE_TIME);
    }

    @Test
    public void testCreate() throws UnknownHostException {
        CreateShortUrlRequest request = getShortUrlRequest(0);
        CreateShortUrlResponse response = createService.create(request);

        LocalDateTime created = LocalDateTime.now(clock);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getShortUrl()), "Short url should not be empty");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getDeleteToken()), "Delete token should not be empty");

        Map<String, String> allParams = unifiedJedis.hgetAll(redisUrlKeys.urlHashKey(response.getShortUrl()));

        Assertions.assertFalse(allParams.isEmpty(), "We should have parameters in redis");
        Assertions.assertEquals(response.getShortUrl(), allParams.get("shortUrl"), "Short url must be equal");
        Assertions.assertEquals(response.getDeleteToken(), allParams.get("deleteToken"), "Delete token must be equal");
        Assertions.assertEquals(request.getOriginalUrl(), allParams.get("originalUrl"), "Original url must be equal");
        Assertions.assertEquals(created.format(DateTimeFormatter.ISO_DATE_TIME), allParams.get("created"));
        Assertions.assertEquals(LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME), allParams.get("expire"), "Expire must be max local date time");
        Assertions.assertEquals("0", allParams.get("visitors"), "Visitors must be 0");
        Assertions.assertEquals(response.getShortUrl(), unifiedJedis.zrange(redisUrlKeys.mostViewedUrlSortedSetKey(), 0, 1).get(0));
        Assertions.assertEquals("1", unifiedJedis.get(redisUrlKeys.getTotalCountKey()));
        Assertions.assertTrue(unifiedJedis.smembers(redisUrlKeys.urlUserAgentsSet(response.getShortUrl())).contains("testUserAgent"));
    }

    @Test
    public void testCreateEmptyUserAgent() throws UnknownHostException {
        testEmptyUserAgent("");
    }

    @Test
    public void testNullUserAgent() throws UnknownHostException {
        testEmptyUserAgent(null);
    }

    private void testEmptyUserAgent(String agent) throws UnknownHostException {
        CreateShortUrlRequest request = new CreateShortUrlRequest(
                "http://test.com",
                0,
                agent,
                InetAddress.getByName("10.10.0.0")
        );

        CreateShortUrlResponse response = createService.create(request);
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getShortUrl()), "Short url should not be empty");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getDeleteToken()), "Delete token should not be empty");

        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.urlUserAgentsSet(response.getShortUrl())));
        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.mostUsedUserAgentsKey()));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 5, 10})
    public void testCreateNUrls(int iterations) throws UnknownHostException {
        List<String> shortUrls = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            CreateShortUrlRequest request = getShortUrlRequest(0);
            CreateShortUrlResponse response = createService.create(request);
            shortUrls.add(response.getShortUrl());

            LocalDateTime created = LocalDateTime.now(clock);

            Assertions.assertNotNull(response, "Response should not be null");
            Assertions.assertTrue(StringUtils.isNotEmpty(response.getShortUrl()), "Short url should not be empty");
            Assertions.assertTrue(StringUtils.isNotEmpty(response.getDeleteToken()), "Delete token should not be empty");

            Map<String, String> allParams = unifiedJedis.hgetAll(redisUrlKeys.urlHashKey(response.getShortUrl()));

            Assertions.assertFalse(allParams.isEmpty(), "We should have parameters in redis");
            Assertions.assertEquals(response.getShortUrl(), allParams.get("shortUrl"), "Short url must be equal");
            Assertions.assertEquals(response.getDeleteToken(), allParams.get("deleteToken"), "Delete token must be equal");
            Assertions.assertEquals(request.getOriginalUrl(), allParams.get("originalUrl"), "Original url must be equal");
            Assertions.assertEquals(created.format(DateTimeFormatter.ISO_DATE_TIME), allParams.get("created"));
            Assertions.assertEquals(LocalDateTime.MAX.format(DateTimeFormatter.ISO_DATE_TIME), allParams.get("expire"), "Expire must be max local date time");
            Assertions.assertEquals("0", allParams.get("visitors"), "Visitors must be 0");
        }
        Assertions.assertTrue(unifiedJedis.zrange(redisUrlKeys.mostViewedUrlSortedSetKey(), 0, iterations + 1).containsAll(shortUrls));
        Assertions.assertEquals(1, unifiedJedis.zcard(redisUrlKeys.mostUsedUserAgentsKey()));
        Assertions.assertEquals(iterations, unifiedJedis.zscore(redisUrlKeys.mostUsedUserAgentsKey(), "testUserAgent"));
        Assertions.assertEquals(Integer.toString(iterations), unifiedJedis.get(redisUrlKeys.getTotalCountKey()));
    }

    @Test
    public void testCreateWithExpire() throws UnknownHostException {
        final int expire = 2;
        CreateShortUrlRequest request = getShortUrlRequest(expire);
        CreateShortUrlResponse response = createService.create(request);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getShortUrl()), "Short url should not be empty");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getDeleteToken()), "Delete token should not be empty");

        try {
            Thread.sleep((expire + 1) * 1000L);
        } catch (InterruptedException e) {
            Assertions.fail(e);
        }

        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.urlHashKey(response.getShortUrl())));
        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.urlVisitedTimeSeriesKey(response.getShortUrl())));
        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.urlUserAgentsSet(response.getShortUrl())));
        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.urlLastVisitedKey(response.getShortUrl())));
        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.getUniqueVisitorsKey(response.getShortUrl())));

    }

    @NotNull
    private static CreateShortUrlRequest getShortUrlRequest(int expire) throws UnknownHostException {
        return new CreateShortUrlRequest(
                "http://test.com",
                expire,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );
    }
}
