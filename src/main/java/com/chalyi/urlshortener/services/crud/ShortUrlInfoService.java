package com.chalyi.urlshortener.services.crud;

import com.chalyi.urlshortener.model.ShortUrl;

public interface ShortUrlInfoService {
    ShortUrl info(String shortUrl);
}
