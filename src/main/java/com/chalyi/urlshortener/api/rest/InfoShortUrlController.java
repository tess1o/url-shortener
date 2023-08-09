package com.chalyi.urlshortener.api.rest;

import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.ShortUrlWithStatistics;
import com.chalyi.urlshortener.services.crud.ShortUrlInfoService;
import com.chalyi.urlshortener.services.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InfoShortUrlController {

    private final ShortUrlInfoService urlManagementService;
    private final StatisticsService statisticsService;

    @RequestMapping(method = RequestMethod.GET, value = "/info/{shortUrl}")
    public ResponseEntity<?> info(@PathVariable("shortUrl") String shortUrl) {
        try {
            ShortUrl info = urlManagementService.info(shortUrl);
            return ResponseEntity.ok(info);
        } catch (NoSuchUrlFound e) {
            String message = "No short url %s found".formatted(shortUrl);
            log.error(message, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(message);
        } catch (Exception e1) {
            log.error("Unable to get info for short url {}", shortUrl, e1);
            return ResponseEntity.internalServerError().body("Internal error, unable to get info for short url " + shortUrl);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/info/stats/{shortUrl}")
    public ResponseEntity<?> infoWithStats(@PathVariable("shortUrl") String shortUrl) {

        try {
            ShortUrlWithStatistics info = statisticsService.getShortUrlWithStatistics(shortUrl);
            return ResponseEntity.ok(info);
        } catch (NoSuchUrlFound e) {
            String message = "No short url %s found".formatted(shortUrl);
            log.error(message, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(message);
        } catch (Exception e1) {
            log.error("Unable to get info for short url {}", shortUrl, e1);
            return ResponseEntity.internalServerError().body("Internal error, unable to get info for short url " + shortUrl);
        }
    }
}
