package com.chalyi.urlshortener.api.rest;


import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.TestDirtyContext;
import com.chalyi.urlshortener.api.rest.dto.CreateShortUrlRequestDto;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestDirtyContext
public class CreateShortUrlControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlInfoService infoService;

    @Test
    public void createShouldReturnResponse() throws Exception {

        final String originalUrl = "http://ukr.net";
        final int expire = 0;
        final String userAgent = "someUserAgent";

        CreateShortUrlRequestDto createShortUrlRequestDto = new CreateShortUrlRequestDto(originalUrl, expire);

        String responseBody = this.mockMvc.perform(post("/create")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header(HttpHeaders.USER_AGENT, userAgent)
                        .content(OBJECT_MAPPER.writeValueAsString(createShortUrlRequestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assertions.assertTrue(StringUtils.isNotBlank(responseBody));
        CreateShortUrlResponse createShortUrlResponse = OBJECT_MAPPER.readValue(responseBody, CreateShortUrlResponse.class);

        ShortUrl shortUrl = infoService.info(createShortUrlResponse.getShortUrl());

        Assertions.assertNotNull(shortUrl);
        Assertions.assertEquals(shortUrl.shortUrl(), createShortUrlResponse.getShortUrl());
        Assertions.assertEquals(originalUrl, shortUrl.originalUrl());
        Assertions.assertEquals(userAgent, shortUrl.userAgents().iterator().next());
    }
}
