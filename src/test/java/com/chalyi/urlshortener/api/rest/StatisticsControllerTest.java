package com.chalyi.urlshortener.api.rest;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.model.OverallStatistics;
import com.chalyi.urlshortener.model.TimeStatistics;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MockMvc;

import java.net.InetAddress;
import java.util.Collections;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTestWithDirtyContext
public class StatisticsControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlCreateService createService;

    @Value("${short-url.statistics.defaultMostViewed}")
    private int defaultMostViewed;

    @Value("${short-url.statistics.defaultMostUsedAgents}")
    private int defaultMostUsedAgents;

    @AfterEach
    public void cleanup() {
        flushAll();
    }

    static Stream<Arguments> counts() {
        return Stream.of(
                Arguments.of(3, 5),
                Arguments.of(5, 3)
        );
    }

    static Stream<Arguments> countsForUserAgents() {
        return Stream.of(
                //count - get param
                //unique userAgents
                //cycles to create urls
                Arguments.of(3, 2, 3),
                Arguments.of(4, 5, 4)
        );
    }

    @Test
    void testMostViewed_emptyDatabase() throws Exception {
        int count = 5;
        this.mockMvc.perform(get("/stats/mostViewed").param("count", String.valueOf(count)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @MethodSource("counts")
    void testMostViewed(int count, int urlsToCreate) throws Exception {
        for (int i = 0; i < urlsToCreate; i++) {
            final String originalUrl = "http://ukr.net";
            final int expire = 0;
            final String userAgent = "someUserAgent";

            createService.create(new CreateShortUrlRequest(
                    originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
            ));
        }

        this.mockMvc.perform(get("/stats/mostViewed").param("count", String.valueOf(count)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(Math.min(count, urlsToCreate)));
    }

    @Test
    void testMostViewed_defaultCount() throws Exception {
        int urlsToCreate = defaultMostViewed + 5;
        for (int i = 0; i < urlsToCreate; i++) {
            final String originalUrl = "http://ukr.net";
            final int expire = 0;
            final String userAgent = "someUserAgent";

            createService.create(new CreateShortUrlRequest(
                    originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
            ));
        }

        this.mockMvc.perform(get("/stats/mostViewed"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(defaultMostViewed));
    }

    @Test
    void testMostUsedAgents_emptyDatabase() throws Exception {
        int count = 5;
        this.mockMvc.perform(get("/stats/mostUsedAgents").param("count", String.valueOf(count)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @MethodSource("countsForUserAgents")
    void testMostUsedAgents(int count, int userAgentsToUse, int cycles) throws Exception {
        for (int c = 0; c < cycles; c++) {
            for (int i = 0; i < userAgentsToUse; i++) {
                final String originalUrl = "http://ukr.net";
                final int expire = 0;
                final String userAgent = "someUserAgent_%d".formatted(i);

                createService.create(new CreateShortUrlRequest(
                        originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
                ));
            }
        }

        this.mockMvc.perform(get("/stats/mostUsedAgents").param("count", String.valueOf(count)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(Math.min(count, userAgentsToUse)));
    }

    @Test
    void testMostUsedAgents_defaultCount() throws Exception {
        int userAgentsToUse = defaultMostUsedAgents + 5;
        for (int i = 0; i < userAgentsToUse; i++) {
            final String originalUrl = "http://ukr.net";
            final int expire = 0;
            final String userAgent = "someUserAgent_%d".formatted(i);

            createService.create(new CreateShortUrlRequest(
                    originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
            ));
        }

        this.mockMvc.perform(get("/stats/mostUsedAgents"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(defaultMostUsedAgents));
    }

    @Test
    void overall_emptyDatabase() throws Exception {
        TimeStatistics timeStatistics = TimeStatistics.builder()
                .build();
        OverallStatistics overallStatistics = OverallStatistics.builder()
                .totalCreated(0)
                .totalVisited(0)
                .averageCreatedPerDay(0)
                .averageVisitedPerDay(0)
                .created(timeStatistics)
                .visited(timeStatistics)
                .mostUsedUserAgents(Collections.emptyList())
                .mostViewed(Collections.emptyList())
                .build();
        this.mockMvc.perform(get("/stats/overall"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(OBJECT_MAPPER.writeValueAsString(overallStatistics)));
    }

    @Test
    void overall() throws Exception {
        int urlsToCreate = 7;
        for (int i = 0; i < urlsToCreate; i++) {
            final String originalUrl = "http://ukr.net";
            final int expire = 0;
            final String userAgent = "someUserAgent";

            createService.create(new CreateShortUrlRequest(
                    originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
            ));
        }

        this.mockMvc.perform(get("/stats/overall"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalCreated").value(String.valueOf(urlsToCreate)))
                .andExpect(jsonPath("created.thisWeek").value(String.valueOf(urlsToCreate)))
                .andExpect(jsonPath("created.today").value(String.valueOf(urlsToCreate)))
                .andExpect(jsonPath("mostUsedUserAgents.length()").value("1"))
                .andExpect(jsonPath("mostViewed.length()").value(Math.min(urlsToCreate, defaultMostViewed)))
                .andExpect(jsonPath("totalCreated").value(String.valueOf(urlsToCreate)));
    }
}
