package com.example.grpc.client;

import com.example.proto.HelloGrpc;
import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloGrpcClientCaller {

    private ManagedChannel channel;
    private HelloGrpc.HelloBlockingStub blockingStub;
    private static final Logger logger = LoggerFactory.getLogger(HelloGrpcClientCaller.class);

    public HelloGrpcClientCaller(ManagedChannel chl) {
        channel = chl;
        blockingStub = HelloGrpc.newBlockingStub(channel);
    }

    /**
     * IDL로 정의한 Stub으로 서버에 request
     */
    public void sendUnaryBlocking() {
        HelloRequest request = HelloRequest.newBuilder()
            .setName("rotomoo") // .proto에 정의한 request value
            .setAge(32) // .proto에 정의한 request value
            .setMessage("client -> server Request") // .proto에 정의한 request value
            .build();

        HelloResponse response = blockingStub.sayHello(request);

        // server -> client response
        logger.info("\n=== Response Data \n{}\n===", response);
    }
}
