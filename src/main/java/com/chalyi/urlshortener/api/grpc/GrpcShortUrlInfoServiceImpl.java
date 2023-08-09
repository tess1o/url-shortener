package com.chalyi.urlshortener.api.grpc;

import com.chalyi.urlshortener.api.grpc.converters.ShortUrlToGrpcShortUrlConverter;
import com.chalyi.urlshortener.api.grpc.converters.ShortUrlWithStatisticsToGrpcShortUrlWithStatisticsConverter;
import com.chalyi.urlshortener.exceptions.NoSuchUrlFound;
import com.chalyi.urlshortener.grpc.ShortUrl;
import com.chalyi.urlshortener.grpc.ShortUrlInfoServiceGrpc;
import com.chalyi.urlshortener.grpc.ShortUrlRequest;
import com.chalyi.urlshortener.grpc.ShortUrlWithStatistics;
import com.chalyi.urlshortener.services.crud.ShortUrlInfoService;
import com.chalyi.urlshortener.services.statistics.StatisticsService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.stereotype.Service;

@GRpcService
@Service
@RequiredArgsConstructor
public class GrpcShortUrlInfoServiceImpl extends ShortUrlInfoServiceGrpc.ShortUrlInfoServiceImplBase {

    private final ShortUrlInfoService shortUrlInfoService;

    private final StatisticsService statisticsService;

    private final ShortUrlToGrpcShortUrlConverter shortUrlToGrpcShortUrlConverter;
    private final ShortUrlWithStatisticsToGrpcShortUrlWithStatisticsConverter shortUrlWithStatisticsToGrpcShortUrlWithStatisticsConverter;

    @Override
    public void info(ShortUrlRequest request, StreamObserver<ShortUrl> responseObserver) {
        try {
            com.chalyi.urlshortener.model.ShortUrl shortUrl = shortUrlInfoService.info(request.getShortUrl());
            ShortUrl response = shortUrlToGrpcShortUrlConverter.convert(shortUrl);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchUrlFound e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Short url " + request.getShortUrl() + " is not found")
                    .asRuntimeException());
        }
    }

    @Override
    public void infoWithStatistics(ShortUrlRequest request, StreamObserver<ShortUrlWithStatistics> responseObserver) {
        try {
            com.chalyi.urlshortener.model.ShortUrlWithStatistics shortUrlWithStatistics = statisticsService.getShortUrlWithStatistics(request.getShortUrl());
            ShortUrlWithStatistics statistics = shortUrlWithStatisticsToGrpcShortUrlWithStatisticsConverter.convert(shortUrlWithStatistics);
            responseObserver.onNext(statistics);
            responseObserver.onCompleted();
        } catch (NoSuchUrlFound e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Short url " + request.getShortUrl() + " is not found")
                    .asRuntimeException());
        }
    }
}
