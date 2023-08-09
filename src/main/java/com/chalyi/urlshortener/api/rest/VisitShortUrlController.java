package com.chalyi.urlshortener.api.rest;

import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;
import com.chalyi.urlshortener.services.crud.ShortUrlVisitService;
import com.chalyi.urlshortener.services.net.HttpRequestIpAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class VisitShortUrlController {

    private final ShortUrlVisitService shortUrlVisitService;
    private final HttpRequestIpAddressService httpRequestIpAddressService;

    @RequestMapping(method = RequestMethod.GET, path = "/{shortUrl}")
    public ResponseEntity<?> redirect(@PathVariable("shortUrl") String shortUrl,
                                      @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                                      HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse) throws IOException {
        VisitShortUrlRequest visitShortUrlRequest = new VisitShortUrlRequest(
                shortUrl, userAgent, httpRequestIpAddressService.getIpAddress(httpServletRequest)
        );

        try {
            String redirectUrl = shortUrlVisitService.visitShortUrl(visitShortUrlRequest);
            httpServletResponse.sendRedirect(redirectUrl);
        } catch (NoSuchUrlFound e) {
            return ResponseEntity.notFound().build();
        }
        return null;
    }
}
