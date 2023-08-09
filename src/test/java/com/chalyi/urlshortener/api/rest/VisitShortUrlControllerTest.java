package com.chalyi.urlshortener.api.rest;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.InetAddress;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTestWithDirtyContext
public class VisitShortUrlControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlCreateService createService;

    @BeforeEach
    public void cleanup() {
        flushAll();
    }

    @Test
    void testVisit() throws Exception {

        final String originalUrl = "http://ukr.net";
        final int expire = 0;
        final String userAgent = "someUserAgent";

        CreateShortUrlResponse createShortUrlResponse = createService.create(new CreateShortUrlRequest(
                originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
        ));

        MvcResult result = this.mockMvc.perform(get("/{shortUrl}", createShortUrlResponse.getShortUrl()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Assertions.assertEquals(originalUrl, result.getResponse().getHeader("Location"));
        Assertions.assertEquals(originalUrl, result.getResponse().getRedirectedUrl());
    }

    @Test
    void testVisit_nonExistingUrl() throws Exception {
        this.mockMvc.perform(get("/{shortUrl}", "non-existing-url"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
