package com.example.grpc.unary;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class HelloGrpcClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 8080)
            .usePlaintext()
            .build();

        HelloGrpcClientCaller helloGrpcClientCaller = new HelloGrpcClientCaller(channel);
        helloGrpcClientCaller.sendUnaryBlocking();
    }
}
