package com.example.grpc.server;

import com.example.proto.HelloGrpc;
import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 서버 동작 코드 (클라이언트에서 요청을 받음)
 */
public class HelloGrpcServiceImpl extends HelloGrpc.HelloImplBase {

    private static final Logger logger = LoggerFactory.getLogger(HelloGrpcServiceImpl.class);

    /**
     * Unary RPC
     */
    @Override
    public void sayHello(HelloRequest helloRequest, StreamObserver<HelloResponse> responseObserver) {
        // client -> server request
        logger.info("\n=== Unary Request Data \n{}===\n", helloRequest);

        // 응답 데이터 셋업
        HelloResponse helloResponse = HelloResponse.newBuilder()
            .setGreetingMessage("hi, " + helloRequest.getName() + " age:" + helloRequest.getAge())  // .proto에 정의한 response value
            .setQuestionMessage("server -> client Response arrived?")  // .proto에 정의한 response value
            .build();

        responseObserver.onNext(helloResponse);
        responseObserver.onCompleted();
    }

    /**
     * Server Streaming RPC
     */
    @Override
    public void lotsOfReplies(HelloRequest helloRequest, StreamObserver<HelloResponse> responseObserver) {
        logger.info("\n=== Server Streaming Request Data \n{}===\n", helloRequest);

        for (int i=1; i<=5; i++) {
            HelloResponse helloResponse = HelloResponse.newBuilder()
                .setGreetingMessage(i + ". " + "hi, " + helloRequest.getName() + " age:" + helloRequest.getAge())
                .setQuestionMessage(i + ". " + "server -> client Response arrived?")
                .build();

            responseObserver.onNext(helloResponse);
        }

        responseObserver.onCompleted();
    }

    /**
     * Client Streaming RPC
     */
    @Override
    public StreamObserver<HelloRequest> lotsOfGreetings(StreamObserver<HelloResponse> responseObserver) {
        return new StreamObserver<HelloRequest>() {
            StringBuilder sb = new StringBuilder();

            @Override
            public void onNext(HelloRequest helloRequest) {
                logger.info("\n=== Client Streaming Request Data \n{}===", helloRequest);
                sb.append("[").append("hi, " + helloRequest.getName() + " age:" + helloRequest.getAge()).append("] ");
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("\n=== Client Streaming Error \n{}===", t.getMessage());
            }

            @Override
            public void onCompleted() {
                HelloResponse helloResponse = HelloResponse.newBuilder()
                    .setGreetingMessage("hi, " + sb)
                    .setQuestionMessage("Server received all requests.")
                    .build();

                responseObserver.onNext(helloResponse);
                responseObserver.onCompleted();
            }
        };
    }
}
