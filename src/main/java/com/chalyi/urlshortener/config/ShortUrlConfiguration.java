package com.chalyi.urlshortener.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "short-url")
@Configuration
@Getter
@Setter
public class ShortUrlConfiguration {
    private int urlLength;
    private int removeTokenLength;
}
