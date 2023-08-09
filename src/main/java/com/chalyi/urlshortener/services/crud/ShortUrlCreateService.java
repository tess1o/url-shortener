package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;

public interface ShortUrlCreateService {
    CreateShortUrlResponse create(CreateShortUrlRequest request);

}
