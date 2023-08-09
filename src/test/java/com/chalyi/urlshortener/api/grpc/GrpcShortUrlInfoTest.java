package com.chalyi.urlshortener.api.grpc;

import com.chalyi.urlshortener.TestDirtyContext;
import com.chalyi.urlshortener.grpc.*;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.model.responses.CreateShortUrlResponse;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;

@TestDirtyContext
public class GrpcShortUrlInfoTest extends BaseGrpcTest {
    @Autowired
    private ShortUrlCreateService createService;

    @AfterEach
    public void cleanup() {
        flushAll();
    }

    @Test
    public void testInfo() throws UnknownHostException {
        final String originalUrl = "http://ukr.net";
        final int expire = 0;
        final String userAgent = "someUserAgent";

        CreateShortUrlResponse createShortUrlResponse = createService.create(new CreateShortUrlRequest(
                originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
        ));

        ShortUrlInfoServiceGrpc.ShortUrlInfoServiceBlockingStub stub = ShortUrlInfoServiceGrpc.newBlockingStub(getChannel());

        ShortUrl info = stub.info(ShortUrlRequest.newBuilder().setShortUrl(createShortUrlResponse.getShortUrl()).build());

        Assertions.assertEquals(originalUrl, info.getOriginalUrl());
        Assertions.assertEquals(createShortUrlResponse.getShortUrl(), info.getShortUrl());
        Assertions.assertEquals(userAgent, info.getUserAgents(0));
    }

    @Test
    public void testInfo_NotExistingUrl() {
        ShortUrlInfoServiceGrpc.ShortUrlInfoServiceBlockingStub stub = ShortUrlInfoServiceGrpc.newBlockingStub(getChannel());

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class,
                () -> stub.info(ShortUrlRequest.newBuilder()
                        .setShortUrl("non-existing-url").build()
                )
        );
        Assertions.assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

    @Test
    public void testInfoWithStatistics() throws UnknownHostException {
        final String originalUrl = "http://ukr.net";
        final int expire = 0;
        final String userAgent = "someUserAgent";

        CreateShortUrlResponse createShortUrlResponse = createService.create(new CreateShortUrlRequest(
                originalUrl, expire, userAgent, InetAddress.getByName("10.1.1.1")
        ));

        ShortUrlInfoServiceGrpc.ShortUrlInfoServiceBlockingStub stub = ShortUrlInfoServiceGrpc.newBlockingStub(getChannel());

        ShortUrlWithStatistics info = stub.infoWithStatistics(ShortUrlRequest.newBuilder().setShortUrl(createShortUrlResponse.getShortUrl()).build());

        Assertions.assertEquals(originalUrl, info.getShortUrl().getOriginalUrl());
        Assertions.assertEquals(createShortUrlResponse.getShortUrl(), info.getShortUrl().getShortUrl());
        Assertions.assertEquals(userAgent, info.getShortUrl().getUserAgents(0));
        Assertions.assertNotNull(info.getVisitedStatistics());
    }

    @Test
    public void testInfoWithStatistics_NotExistingUrl() {
        ShortUrlInfoServiceGrpc.ShortUrlInfoServiceBlockingStub stub = ShortUrlInfoServiceGrpc.newBlockingStub(getChannel());

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class,
                () -> stub.infoWithStatistics(ShortUrlRequest.newBuilder()
                        .setShortUrl("non-existing-url").build()
                )
        );
        Assertions.assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

}
