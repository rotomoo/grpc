package com.example.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class HelloGrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 클라이언트 요청을 수신하는데 사용할 포트 지정
        Server grpcServer = ServerBuilder
            .forPort(8080)
            .addService(new HelloGrpcServiceImpl())
            .build();

        grpcServer.start();
        grpcServer.awaitTermination();
    }
}
