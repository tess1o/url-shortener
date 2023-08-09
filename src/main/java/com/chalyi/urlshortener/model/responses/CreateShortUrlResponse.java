package com.chalyi.urlshortener.model.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateShortUrlResponse {
    private String shortUrl;
    private String deleteToken;
}
