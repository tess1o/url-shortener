package com.chalyi.urlshortener.services.id;

import com.chalyi.urlshortener.BaseTest;
import com.chalyi.urlshortener.TestDirtyContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

@TestDirtyContext
@Slf4j
public class ShortIdGeneratorTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(ints = {10, 12, 14, 16})
    public void testGenerateId(int length) {
        ShortIdGenerator generator = new ShortIdGeneratorImpl();
        Set<String> ids = new HashSet<>();
        int idsToGenerate = 10_000_000;
        for (int i = 0; i < idsToGenerate; i++) {
            ids.add(generator.generate(length));
        }
        Assertions.assertEquals(idsToGenerate, ids.size(), "We should not have collisions");
    }
}
