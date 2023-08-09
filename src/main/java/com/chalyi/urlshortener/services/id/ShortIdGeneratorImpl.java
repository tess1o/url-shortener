package com.chalyi.urlshortener.services.id;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class ShortIdGeneratorImpl implements ShortIdGenerator {

    @Override
    public String generate(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
}
