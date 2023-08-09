package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.TimeMachine;
import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.RedisUrlKeys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import redis.clients.jedis.UnifiedJedis;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootTestWithDirtyContext
@Slf4j
public class ShortUrlVisitServiceTest extends BaseTest {

    private final static LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2023, 7, 12, 14, 0);

    private final ShortUrlCreateService createService;
    private final ShortUrlVisitService visitService;
    private final UnifiedJedis unifiedJedis;
    private final RedisUrlKeys redisUrlKeys;

    @MockBean
    private Clock clock;

    @Autowired
    public ShortUrlVisitServiceTest(ShortUrlCreateService createService, ShortUrlVisitService visitService, UnifiedJedis unifiedJedis, RedisUrlKeys redisUrlKeys) {
        this.createService = createService;
        this.visitService = visitService;
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
    public void testVisitUrl() throws UnknownHostException {
        CreateShortUrlRequest createRequest = new CreateShortUrlRequest(
                "http://test.com",
                0,
                "testUserAgent"
        );
        CreateShortUrlResponse createShortUrlResponse = createService.create(createRequest);

        Assertions.assertNotNull(createShortUrlResponse);
        Assertions.assertNotNull(createShortUrlResponse.getShortUrl());

        VisitShortUrlRequest visitShortUrlRequest = new VisitShortUrlRequest(
                createShortUrlResponse.getShortUrl(),
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );

        final int visitCount = 5;

        for (int i = 0; i < visitCount; i++) {
            visitService.visitShortUrl(visitShortUrlRequest);
        }

        String urlHashKey = redisUrlKeys.urlHashKey(createShortUrlResponse.getShortUrl());

        Assertions.assertEquals(visitCount, Long.valueOf(unifiedJedis.get(redisUrlKeys.getTotalVisitedCountKey())));
        Assertions.assertEquals(createShortUrlResponse.getShortUrl(), unifiedJedis.zrange(redisUrlKeys.mostViewedUrlSortedSetKey(), 0, 1).get(0));
        Assertions.assertEquals(visitCount, Long.valueOf(unifiedJedis.hget(urlHashKey, "visitors")));
        Assertions.assertTrue(unifiedJedis.smembers(redisUrlKeys.urlUserAgentsSet(createShortUrlResponse.getShortUrl())).contains("testUserAgent"));
        Assertions.assertEquals(1, unifiedJedis.pfcount(redisUrlKeys.getUniqueVisitorsKey(createShortUrlResponse.getShortUrl())));
        Assertions.assertEquals(LOCAL_DATE_TIME, fromUnixTimestamp(unifiedJedis.tsGet(redisUrlKeys.urlVisitedTimeSeriesKey(createShortUrlResponse.getShortUrl())).getTimestamp()));
        Assertions.assertEquals(LOCAL_DATE_TIME, fromUnixTimestamp(unifiedJedis.tsGet(redisUrlKeys.urlVisitedTimeSeriesKey()).getTimestamp()));
        Assertions.assertEquals(visitCount, unifiedJedis.lrange(redisUrlKeys.urlLastVisitedKey(createShortUrlResponse.getShortUrl()), -10, -1).size());

        visitShortUrlRequest.setIpAddress(InetAddress.getByName("10.10.0.1"));
        visitService.visitShortUrl(visitShortUrlRequest);

        Assertions.assertEquals(2, unifiedJedis.pfcount(redisUrlKeys.getUniqueVisitorsKey(createShortUrlResponse.getShortUrl())));

        final int newVisitCount = 15;

        for (int i = 0; i < newVisitCount; i++) {
            visitService.visitShortUrl(visitShortUrlRequest);
        }

        Assertions.assertEquals(10, unifiedJedis.lrange(redisUrlKeys.urlLastVisitedKey(createShortUrlResponse.getShortUrl()), -10, -1).size());

        LocalDateTime newNow = LOCAL_DATE_TIME.plusMinutes(1);
        new TimeMachine(clock).setFixedClock(newNow);

        visitService.visitShortUrl(visitShortUrlRequest);

        Assertions.assertEquals(newNow, fromUnixTimestamp(unifiedJedis.tsGet(redisUrlKeys.urlVisitedTimeSeriesKey(createShortUrlResponse.getShortUrl())).getTimestamp()));
        Assertions.assertEquals(newNow, fromUnixTimestamp(unifiedJedis.tsGet(redisUrlKeys.urlVisitedTimeSeriesKey()).getTimestamp()));


    }

    @Test
    public void testVisitUrl_NonExistingUrl() throws UnknownHostException {

        VisitShortUrlRequest visitShortUrlRequest = new VisitShortUrlRequest(
                "non-existing-url",
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );
        Assertions.assertThrows(NoSuchUrlFound.class, () -> visitService.visitShortUrl(visitShortUrlRequest));
    }

    @Test
    public void testVisitUrl_emptyUserAgent() throws UnknownHostException {
        testVisitUrlUserAgents("");
    }

    @Test
    public void testVisitUrl_NullUserAgent() throws UnknownHostException {
        testVisitUrlUserAgents(null);
    }

    private void testVisitUrlUserAgents(String visitUserAgent) throws UnknownHostException {
        CreateShortUrlRequest createRequest = new CreateShortUrlRequest(
                "http://test.com",
                0,
                "userAgent"
        );
        CreateShortUrlResponse createShortUrlResponse = createService.create(createRequest);

        Assertions.assertNotNull(createShortUrlResponse);
        Assertions.assertNotNull(createShortUrlResponse.getShortUrl());

        VisitShortUrlRequest visitShortUrlRequest = new VisitShortUrlRequest(
                createShortUrlResponse.getShortUrl(),
                visitUserAgent,
                InetAddress.getByName("10.10.0.0")
        );

        visitService.visitShortUrl(visitShortUrlRequest);


        Assertions.assertEquals(1, unifiedJedis.smembers(redisUrlKeys.urlUserAgentsSet(createShortUrlResponse.getShortUrl())).size());
        Assertions.assertTrue(unifiedJedis.smembers(redisUrlKeys.urlUserAgentsSet(createShortUrlResponse.getShortUrl())).contains("userAgent"));
    }

    private LocalDateTime fromUnixTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                TimeZone.getDefault().toZoneId());
    }


}
