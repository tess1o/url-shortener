package com.chalyi.urlshortener.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateShortUrlRequest {
    private String originalUrl;
    private long expire;
    private String userAgent;
    private InetAddress inetAddress;
}
