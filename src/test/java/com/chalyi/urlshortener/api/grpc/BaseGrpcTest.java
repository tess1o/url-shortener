package com.chalyi.urlshortener.api.grpc;

import com.chalyi.urlshortener.BaseTest;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.lognet.springboot.grpc.context.LocalRunningGrpcPort;

public abstract class BaseGrpcTest extends BaseTest {

    @LocalRunningGrpcPort
    protected int runningPort;

    protected int getPort() {
        return runningPort;
    }

    protected Channel getChannel(){
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress("localhost", getPort());
        channelBuilder.usePlaintext();
        return channelBuilder.build();
    }
}
