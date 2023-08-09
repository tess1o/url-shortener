package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.TestDirtyContext;
import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

@TestDirtyContext
@Slf4j
public class ShortUrlInfoServiceTest extends BaseTest {

    private final ShortUrlCreateService createService;
    private final ShortUrlInfoService infoService;

    @Autowired
    public ShortUrlInfoServiceTest(ShortUrlCreateService createService, ShortUrlInfoService infoService) {
        this.createService = createService;
        this.infoService = infoService;
    }

    @Test
    public void testInfo() throws UnknownHostException {
        CreateShortUrlRequest request = new CreateShortUrlRequest(
                "http://test.com",
                0,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );

        CreateShortUrlResponse shortUrlResponse = createService.create(request);
        ShortUrl shortUrl = infoService.info(shortUrlResponse.getShortUrl());
        Assertions.assertEquals(request.getOriginalUrl(), shortUrl.originalUrl());
        Assertions.assertEquals(shortUrlResponse.getShortUrl(), shortUrl.shortUrl());
        Assertions.assertEquals(LocalDateTime.MAX, shortUrl.expire());
        Assertions.assertEquals(1, shortUrl.userAgents().size());
        Assertions.assertTrue(shortUrl.userAgents().contains(request.getUserAgent()));
        Assertions.assertEquals(0, shortUrl.uniqueVisitors());
    }

    @Test
    public void testInfo_NotExistingUrl() {
        Assertions.assertThrows(NoSuchUrlFound.class, () -> infoService.info("non-existing-url"));
    }
}
