package com.chalyi.urlshortener.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitShortUrlRequest {
    private String shortUrl;
    private String userAgent;
    private InetAddress ipAddress;
}
