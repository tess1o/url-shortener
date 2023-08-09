package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.model.requests.VisitShortUrlRequest;

public interface ShortUrlVisitService {
    String visitShortUrl(VisitShortUrlRequest request);
}
