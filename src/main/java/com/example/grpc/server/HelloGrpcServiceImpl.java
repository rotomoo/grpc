package com.example.grpc;

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
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        logger.info("\n=== Request Data \n{}\n===", request);

        // 응답 데이터 셋업
        HelloResponse response = HelloResponse.newBuilder()
            .setGreetingMessage("hi, " + request.getName() + " age:" + request.getAge())  // .proto에 정의한 response value
            .setQuestionMessage("server -> client Response arrived?")  // .proto에 정의한 response value
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
