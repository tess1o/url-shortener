package com.chalyi.urlshortener.api.grpc;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;

import java.net.SocketAddress;

@GRpcGlobalInterceptor
@Slf4j
public class ClientIpAddressGrpcInterceptor implements ServerInterceptor {

    public static final Context.Key<SocketAddress> CLIENT_IP_ADDRESS_KEY = Context.key("client.ip.address");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        SocketAddress address = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        Context current = Context.current().withValue(CLIENT_IP_ADDRESS_KEY, address);
        return Contexts.interceptCall(current, call, headers, next);
    }
}
