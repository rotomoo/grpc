package com.example.grpc.service;

import com.example.proto.HelloGrpc;
import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j  // 로그를 사용하기 위해 추가
public class HelloGrpcServiceImpl extends HelloGrpc.HelloImplBase {

    // Unary RPC (단방향 RPC)
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        log.info("=== Get Request");
        log.info("=== Request Data => [%s]".formatted(request));

        // 응답 데이터 셋업
        HelloResponse response = HelloResponse.newBuilder()
            .setGreetingMessage("Hello, %s".formatted(request.getName()))  // .proto에 정의한 response value
            .setQuestionMessage("What do you do for fun?")  // .proto에 정의한 response value
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
