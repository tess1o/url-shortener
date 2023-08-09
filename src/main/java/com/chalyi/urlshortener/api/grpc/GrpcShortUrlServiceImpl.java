package com.chalyi.urlshortener.api.grpc;

import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.exceptions.WrongDeleteTokenException;
import com.chalyi.urlshortener.grpc.*;
import com.chalyi.urlshortener.services.crud.ShortUrlCreateService;
import com.chalyi.urlshortener.services.crud.ShortUrlDeleteService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static com.chalyi.urlshortener.api.grpc.ClientIpAddressGrpcInterceptor.CLIENT_IP_ADDRESS_KEY;

@GRpcService
@Service
@RequiredArgsConstructor
public class GrpcShortUrlServiceImpl extends ShortUrlServiceGrpc.ShortUrlServiceImplBase {

    private final ShortUrlCreateService createService;
    private final ShortUrlDeleteService deleteService;

    @Override
    public void create(CreateShortUrlRequest request, StreamObserver<CreateShortUrlResponse> responseObserver) {
        InetAddress clientIp = getClientIp();
        com.chalyi.urlshortener.model.requests.CreateShortUrlRequest createRequest = new com.chalyi.urlshortener.model.requests.CreateShortUrlRequest(
                request.getOriginalUrl(),
                request.getExpire(),
                "gRPC",
                clientIp
        );
        com.chalyi.urlshortener.model.responses.CreateShortUrlResponse response = createService.create(createRequest);
        responseObserver.onNext(CreateShortUrlResponse.newBuilder()
                .setShortUrl(response.getShortUrl())
                .setDeleteToken(response.getDeleteToken())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteShortUrlRequest request, StreamObserver<DeleteShortUrlResponse> responseObserver) {
        try {
            deleteService.delete(request.getShortUrl(), request.getDeleteToken());
            responseObserver.onNext(DeleteShortUrlResponse.newBuilder()
                    .setResponse("Deleted")
                    .build());
            responseObserver.onCompleted();
        } catch (WrongDeleteTokenException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Wrong Delete Token for url: " + request.getShortUrl())
                    .asRuntimeException());
        } catch (NoSuchUrlFound noSuchUrlFound) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("No such url found " + request.getShortUrl())
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private InetAddress getClientIp() {
        Object clientIpAddress = CLIENT_IP_ADDRESS_KEY.get();
        return clientIpAddress instanceof InetSocketAddress ? ((InetSocketAddress) clientIpAddress).getAddress() : null;
    }
}
