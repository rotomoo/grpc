package com.example.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class HelloGrpcClient {

    public static void main(String[] args) {
        // 스텁(Stub)에 대한 gRPC 채널 생성 및 연결 서버 주소와 포트 지정
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 8080)
            .usePlaintext()
            .build();

        HelloGrpcClientCaller helloGrpcClientCaller = new HelloGrpcClientCaller(channel);
        helloGrpcClientCaller.sendUnaryBlocking();
        helloGrpcClientCaller.sendServerStreamingBlocking();
        helloGrpcClientCaller.sendClientStreamingAsync();
    }
}
