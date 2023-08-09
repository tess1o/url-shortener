package com.chalyi.urlshortener.api.rest;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.TestDirtyContext;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.ShortUrlWithStatistics;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlInfoService;
import com.chalyi.urlshortener.services.statistics.StatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.InetAddress;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestDirtyContext
public class InfoShortUrlControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlCreateService createService;

    @SpyBean
    private ShortUrlInfoService infoService;

    @Autowired
    private StatisticsService statisticsService;

    @BeforeEach
    public void cleanup() {
        flushAll();
    }

    @Test
    public void infoShouldReturnResults() throws Exception {

        final String originalUrl = "http://ukr.net";
        final int expire = 0;
        final String userAgent = "someUserAgent";

        CreateShortUrlResponse createShortUrlResponse = createService.create(new CreateShortUrlRequest(
                originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
        ));

        MvcResult result = this.mockMvc.perform(get("/info/{shortUrl}", createShortUrlResponse.getShortUrl()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("originalUrl").value(originalUrl))
                .andExpect(jsonPath("shortUrl").value(createShortUrlResponse.getShortUrl()))
                .andExpect(jsonPath("userAgents").isArray())
                .andExpect(jsonPath("userAgents", Matchers.contains(userAgent)))
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ShortUrl fromController = objectMapper.readValue(result.getResponse().getContentAsString(), ShortUrl.class);
        ShortUrl fromService = infoService.info(createShortUrlResponse.getShortUrl());

        Assertions.assertEquals(fromService, fromController);
    }

    @Test
    public void infoWithStatsShouldReturnResults() throws Exception {

        final String originalUrl = "http://ukr.net";
        final int expire = 0;
        final String userAgent = "someUserAgent";

        CreateShortUrlResponse createShortUrlResponse = createService.create(new CreateShortUrlRequest(
                originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
        ));

        MvcResult result = this.mockMvc.perform(get("/info/stats/{shortUrl}", createShortUrlResponse.getShortUrl()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("shortUrl.originalUrl").value(originalUrl))
                .andExpect(jsonPath("shortUrl.shortUrl").value(createShortUrlResponse.getShortUrl()))
                .andExpect(jsonPath("shortUrl.userAgents").isArray())
                .andExpect(jsonPath("shortUrl.userAgents", Matchers.contains(userAgent)))
                .andReturn();

        ShortUrlWithStatistics fromController = OBJECT_MAPPER.readValue(result.getResponse().getContentAsString(), ShortUrlWithStatistics.class);
        ShortUrlWithStatistics fromService = statisticsService.getShortUrlWithStatistics(createShortUrlResponse.getShortUrl());
        Assertions.assertEquals(fromService, fromController);
    }

    @Test
    void infoForNonExistingUrl() throws Exception {
        String nonExistingShortUrl = "non-existing-short-url";
        this.mockMvc.perform(get("/info/{shortUrl}", nonExistingShortUrl))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void infoInternalError() throws Exception {
        Class<IllegalArgumentException> throwableType = IllegalArgumentException.class;

        doThrow(throwableType).when(infoService).info(anyString());

        String someUrl = "someUrl";
        this.mockMvc.perform(get("/info/{shortUrl}", someUrl))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }

    @Test
    void infoWithStatsForNonExistingUrl() throws Exception {
        String nonExistingShortUrl = "non-existing-short-url";
        this.mockMvc.perform(get("/info//stats/{shortUrl}", nonExistingShortUrl))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    void infoWithStatsInternalError() throws Exception {
        Class<IllegalArgumentException> throwableType = IllegalArgumentException.class;

        doThrow(throwableType).when(infoService).info(anyString());

        String someUrl = "someUrl";
        this.mockMvc.perform(get("/info/stats/{shortUrl}", someUrl))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }
}
