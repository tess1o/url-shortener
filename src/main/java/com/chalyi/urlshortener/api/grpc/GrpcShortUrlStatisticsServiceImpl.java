package com.chalyi.urlshortener.api.grpc;

import com.chalyi.urlshortener.api.grpc.converters.MostUserAgentsToMostUserAgentsResponseConverter;
import com.chalyi.urlshortener.api.grpc.converters.OverallStatisticsToGrpcOverallStatisticsConverter;
import com.chalyi.urlshortener.api.grpc.converters.ShortUrlToGrpcShortUrlConverter;
import com.chalyi.urlshortener.grpc.*;
import com.chalyi.urlshortener.model.responses.MostUsedUserAgents;
import com.chalyi.urlshortener.services.statistics.StatisticsService;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.stereotype.Service;

import java.util.List;

@GRpcService
@Service
@RequiredArgsConstructor
public class GrpcShortUrlStatisticsServiceImpl extends UrlStatisticsServiceGrpc.UrlStatisticsServiceImplBase {

    private final StatisticsService statisticsService;

    private final ShortUrlToGrpcShortUrlConverter shortUrlToGrpcShortUrlConverter;
    private final MostUserAgentsToMostUserAgentsResponseConverter mostUserAgentsToMostUserAgentsResponseConverter;
    private final OverallStatisticsToGrpcOverallStatisticsConverter overallStatisticsToGrpcOverallStatisticsConverter;

    @Override
    public void getMostViewedShortUrls(MostViewedUrlRequest request, StreamObserver<ShortUrl> responseObserver) {
        List<com.chalyi.urlshortener.model.ShortUrl> mostViewed = statisticsService.getMostViewed(request.getCount());
        for (com.chalyi.urlshortener.model.ShortUrl shortUrl : mostViewed) {
            responseObserver.onNext(shortUrlToGrpcShortUrlConverter.convert(shortUrl));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getMostUsedUserAgents(MostUserAgentsRequest request, StreamObserver<MostUserAgentsResponse> responseObserver) {
        List<MostUsedUserAgents> mostUsedUserAgents = statisticsService.getMostUsedUserAgents(request.getCount());
        mostUsedUserAgents.stream()
                .map(mostUserAgentsToMostUserAgentsResponseConverter::convert)
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getOverallStatistics(Empty request, StreamObserver<OverallStatistics> responseObserver) {
        com.chalyi.urlshortener.model.OverallStatistics overallStatistics = statisticsService.overallTimeStatistics();
        OverallStatistics converted = overallStatisticsToGrpcOverallStatisticsConverter.convert(overallStatistics);
        responseObserver.onNext(converted);
        responseObserver.onCompleted();
    }
}
