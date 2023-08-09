package com.chalyi.urlshortener.api.rest;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.TestDirtyContext;
import com.chalyi.urlshortener.api.rest.dto.DeleteShortUrlRequestDto;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.InetAddress;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestDirtyContext
public class DeleteShortUrlControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlCreateService createService;

    @BeforeEach
    public void cleanup() {
        flushAll();
    }

    @Test
    void testDelete() throws Exception {

        final String originalUrl = "http://ukr.net";
        final int expire = 0;
        final String userAgent = "someUserAgent";

        CreateShortUrlResponse createShortUrlResponse = createService.create(new CreateShortUrlRequest(
                originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
        ));

        DeleteShortUrlRequestDto deleteRequest =
                new DeleteShortUrlRequestDto(createShortUrlResponse.getShortUrl(), createShortUrlResponse.getDeleteToken());

        this.mockMvc.perform(delete("/delete")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(OBJECT_MAPPER.writeValueAsString(deleteRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"response\":\"Deleted\"}"));
    }

    @Test
    void testDelete_WrongDeleteToken() throws Exception {

        final String originalUrl = "http://ukr.net";
        final int expire = 0;
        final String userAgent = "someUserAgent";

        CreateShortUrlResponse createShortUrlResponse = createService.create(new CreateShortUrlRequest(
                originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
        ));

        DeleteShortUrlRequestDto deleteRequest =
                new DeleteShortUrlRequestDto(createShortUrlResponse.getShortUrl(), "wrong-delete-token");

        this.mockMvc.perform(delete("/delete")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(OBJECT_MAPPER.writeValueAsString(deleteRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDelete_NonExistingShortUrl() throws Exception {

        DeleteShortUrlRequestDto deleteRequest =
                new DeleteShortUrlRequestDto("non-existing-url", "wrong-delete-token");

        this.mockMvc.perform(delete("/delete")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(OBJECT_MAPPER.writeValueAsString(deleteRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
