package com.example.grpc.unary;

import com.example.proto.HelloGrpc;
import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloGrpcClientCaller {

    private ManagedChannel channel;
    private HelloGrpc.HelloBlockingStub blockingStub;

    public HelloGrpcClientCaller(ManagedChannel chl) {
        channel = chl;
        blockingStub = HelloGrpc.newBlockingStub(channel);
    }

    public void sendUnaryBlocking() {
        log.info(">>> Send Call");

        HelloResponse response = blockingStub.sayHello(HelloRequest.newBuilder()
            .setName("herojoon")  // .proto에 정의한 request value
            .setAge(10)  // .proto에 정의한 request value
            .setMessage("Hello, Glad to meet you.")  // .proto에 정의한 request value
            .build());

        log.info(">>> Response Data => [%s]".formatted(response));
    }
}
