package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.TestDirtyContext;
import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.exceptions.WrongDeleteTokenException;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.RedisUrlKeys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.UnifiedJedis;

import java.net.InetAddress;
import java.net.UnknownHostException;

@TestDirtyContext
@Slf4j
public class ShortUrlDeleteServiceTest extends BaseTest {

    private final ShortUrlCreateService createService;

    private final ShortUrlDeleteService deleteService;
    private final UnifiedJedis unifiedJedis;
    private final RedisUrlKeys redisUrlKeys;

    @Autowired
    public ShortUrlDeleteServiceTest(ShortUrlCreateService createService, ShortUrlDeleteService deleteService,
                                     UnifiedJedis unifiedJedis, RedisUrlKeys redisUrlKeys) {
        this.createService = createService;
        this.deleteService = deleteService;
        this.unifiedJedis = unifiedJedis;
        this.redisUrlKeys = redisUrlKeys;
    }

    @BeforeEach
    public void cleanup() {
        flushAll();
    }

    @Test
    public void testDelete() throws UnknownHostException {
        CreateShortUrlResponse response = getCreateShortUrlResponse();
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getShortUrl()), "Short url should not be empty");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getDeleteToken()), "Delete token should not be empty");

        Assertions.assertDoesNotThrow(() -> deleteService.delete(response.getShortUrl(), response.getDeleteToken()), "Delete url with valid token should not fail");

        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.urlHashKey(response.getShortUrl())));
        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.urlVisitedTimeSeriesKey(response.getShortUrl())));
        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.urlUserAgentsSet(response.getShortUrl())));
        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.urlLastVisitedKey(response.getShortUrl())));
        Assertions.assertFalse(unifiedJedis.exists(redisUrlKeys.getUniqueVisitorsKey(response.getShortUrl())));
    }

    @Test
    public void testDeleteNoSuchUrl() {
        Assertions.assertThrows(NoSuchUrlFound.class,
                () -> deleteService.delete("non-existing-url", "non-existing-token"),
                "Delete url with valid token should not fail");
    }

    @Test
    public void testDeleteWrongToken() throws UnknownHostException {
        CreateShortUrlResponse response = getCreateShortUrlResponse();
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getShortUrl()), "Short url should not be empty");
        Assertions.assertTrue(StringUtils.isNotEmpty(response.getDeleteToken()), "Delete token should not be empty");

        Assertions.assertThrows(WrongDeleteTokenException.class,
                () -> deleteService.delete(response.getShortUrl(), "non-existing-token"),
                "Delete url with valid token should not fail"
        );
    }

    private CreateShortUrlResponse getCreateShortUrlResponse() throws UnknownHostException {
        CreateShortUrlRequest request = new CreateShortUrlRequest(
                "http://test.com",
                0,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );
        return createService.create(request);
    }
}
