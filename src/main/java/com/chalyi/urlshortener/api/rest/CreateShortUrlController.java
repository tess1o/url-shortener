package com.chalyi.urlshortener.api.rest;

import com.chalyi.urlshortener.api.rest.dto.CreateShortUrlRequestDto;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.net.HttpRequestIpAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CreateShortUrlController {

    private final ShortUrlCreateService createService;
    private final HttpRequestIpAddressService httpRequestIpAddressService;

    @RequestMapping(path = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateShortUrlResponse create(@Valid @RequestBody CreateShortUrlRequestDto urlRequestDto,
                                         @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
                                         HttpServletRequest httpServletRequest) {
        CreateShortUrlRequest createRequest = new CreateShortUrlRequest(
                urlRequestDto.getOriginalUrl(),
                urlRequestDto.getExpire(),
                userAgent,
                httpRequestIpAddressService.getIpAddress(httpServletRequest)
        );
        return createService.create(createRequest);
    }
}
