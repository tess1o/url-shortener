package com.chalyi.urlshortener.api.grpc;

import com.chalyi.urlshortener.SpringBootTestWithDirtyContext;
import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.grpc.CreateShortUrlResponse;
import com.chalyi.urlshortener.grpc.DeleteShortUrlRequest;
import com.chalyi.urlshortener.grpc.DeleteShortUrlResponse;
import com.chalyi.urlshortener.grpc.ShortUrlServiceGrpc;
import com.chalyi.urlshortener.model.ShortUrl;
import com.chalyi.urlshortener.model.requests.CreateShortUrlRequest;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlDeleteService;
import com.chalyi.urlshortener.services.crud.ShortUrlInfoService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;


@SpringBootTestWithDirtyContext
public class GrpcShortUrlCreateTest extends BaseGrpcTest {

    @Autowired
    private ShortUrlCreateService createService;

    @Autowired
    private ShortUrlInfoService infoService;

    @SpyBean
    private ShortUrlDeleteService deleteService;

    @AfterEach
    public void cleanup() {
        flushAll();
    }

    @Test
    void testCreate() {
        ShortUrlServiceGrpc.ShortUrlServiceBlockingStub stub = ShortUrlServiceGrpc.newBlockingStub(getChannel());

        String originalUrl = "http://ukr.net";
        com.chalyi.urlshortener.grpc.CreateShortUrlRequest request = com.chalyi.urlshortener.grpc.CreateShortUrlRequest.newBuilder()
                .setOriginalUrl(originalUrl)
                .setExpire(0L)
                .build();

        CreateShortUrlResponse shortUrlResponse = stub.create(request);
        ShortUrl info = infoService.info(shortUrlResponse.getShortUrl());
        Assertions.assertNotNull(info);
        Assertions.assertEquals(originalUrl, info.originalUrl());
    }

    @Test
    void testDelete() throws UnknownHostException {
        ShortUrlServiceGrpc.ShortUrlServiceBlockingStub stub = ShortUrlServiceGrpc.newBlockingStub(getChannel());

        CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                "http://test.com",
                0,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );

        com.chalyi.urlshortener.model.responses.CreateShortUrlResponse response = createService.create(createShortUrlRequest);
        ShortUrl info = infoService.info(response.getShortUrl());
        Assertions.assertNotNull(info);
        Assertions.assertEquals(createShortUrlRequest.getOriginalUrl(), info.originalUrl());

        DeleteShortUrlRequest deleteShortUrlRequest = DeleteShortUrlRequest.newBuilder()
                .setShortUrl(response.getShortUrl())
                .setDeleteToken(response.getDeleteToken())
                .build();

        DeleteShortUrlResponse delete = stub.delete(deleteShortUrlRequest);
        Assertions.assertNotNull(delete);
        Assertions.assertEquals("Deleted", delete.getResponse());

        Assertions.assertThrows(NoSuchUrlFound.class, () -> infoService.info(response.getShortUrl()));
    }

    @Test
    void testDelete_WrongToken() throws UnknownHostException {
        ShortUrlServiceGrpc.ShortUrlServiceBlockingStub stub = ShortUrlServiceGrpc.newBlockingStub(getChannel());

        CreateShortUrlRequest createShortUrlRequest = new CreateShortUrlRequest(
                "http://test.com",
                0,
                "testUserAgent",
                InetAddress.getByName("10.10.0.0")
        );

        com.chalyi.urlshortener.model.responses.CreateShortUrlResponse response = createService.create(createShortUrlRequest);
        ShortUrl info = infoService.info(response.getShortUrl());
        Assertions.assertNotNull(info);
        Assertions.assertEquals(createShortUrlRequest.getOriginalUrl(), info.originalUrl());

        DeleteShortUrlRequest deleteShortUrlRequest = DeleteShortUrlRequest.newBuilder()
                .setShortUrl(response.getShortUrl())
                .setDeleteToken("wrong-token-here")
                .build();

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> stub.delete(deleteShortUrlRequest));
        Assertions.assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode());
        Assertions.assertTrue(exception.getMessage().contains(response.getShortUrl()));
    }

    @Test
    void testDelete_NotExistingUrl() {
        ShortUrlServiceGrpc.ShortUrlServiceBlockingStub stub = ShortUrlServiceGrpc.newBlockingStub(getChannel());

        DeleteShortUrlRequest deleteShortUrlRequest = DeleteShortUrlRequest.newBuilder()
                .setShortUrl("wrong-url-here")
                .setDeleteToken("wrong-token-here")
                .build();

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> stub.delete(deleteShortUrlRequest));
        Assertions.assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

    @Test
    void testDelete_UnexpectedError() {
        ShortUrlServiceGrpc.ShortUrlServiceBlockingStub stub = ShortUrlServiceGrpc.newBlockingStub(getChannel());

        doThrow(new NullPointerException()).when(deleteService).delete(anyString(), anyString());

        DeleteShortUrlRequest deleteShortUrlRequest = DeleteShortUrlRequest.newBuilder()
                .setShortUrl("wrong-url-here")
                .setDeleteToken("wrong-token-here")
                .build();

        StatusRuntimeException exception = Assertions.assertThrows(StatusRuntimeException.class, () -> stub.delete(deleteShortUrlRequest));
        Assertions.assertEquals(Status.INTERNAL.getCode(), exception.getStatus().getCode());
    }
}
