package com.example.grpc.client;

import com.example.proto.HelloGrpc;
import com.example.proto.HelloRequest;
import com.example.proto.HelloResponse;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 클라이언트 동작 코드 (서버에 요청)
 */
public class HelloGrpcClientCaller {

    private ManagedChannel channel;
    private HelloGrpc.HelloBlockingStub blockingStub;
    private HelloGrpc.HelloStub asyncStub;
    private static final Logger logger = LoggerFactory.getLogger(HelloGrpcClientCaller.class);

    public HelloGrpcClientCaller(ManagedChannel chl) {
        channel = chl;
        blockingStub = HelloGrpc.newBlockingStub(channel);
        asyncStub = HelloGrpc.newStub(channel);
    }

    /**
     * IDL로 정의한 Stub으로 서버에 Unary RPC
     */
    public void sendUnaryBlocking() {
        HelloRequest request = HelloRequest.newBuilder()
            .setName("rotomoo") // .proto에 정의한 request value
            .setAge(32) // .proto에 정의한 request value
            .setMessage("client -> server Request") // .proto에 정의한 request value
            .build();

        HelloResponse helloResponse = blockingStub.sayHello(request);

        // server -> client response
        logger.info("\n=== Unary Response Data \n{}===\n", helloResponse);
    }

    /**
     * 서버에 Server Streaming RPC
     */
    public void sendServerStreamingBlocking() {
        HelloRequest request = HelloRequest.newBuilder()
            .setName("rotomoo")
            .setAge(32)
            .setMessage("client -> server Request")
            .build();

        Iterator<HelloResponse> helloResponseIterator = blockingStub.lotsOfReplies(request);

        helloResponseIterator.forEachRemaining(helloResponse -> {
            logger.info("\n=== Server Streaming Response Data \n{}===", helloResponse);
        });
    }

    /**
     * 서버에 Client Streaming RPC
     */
    public void sendClientStreamingAsync() {
        // 비동기 방식이기 때문에 서버 Response 를 기다릴 CountDownLatch 추가
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<HelloResponse> responseObserver = new StreamObserver<HelloResponse>() {
            @Override
            public void onNext(HelloResponse helloResponse) {
                logger.info("\n=== Client Streaming Response Data \n{}===", helloResponse);
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<HelloRequest> requestObserver = asyncStub.lotsOfGreetings(responseObserver);

        List<HelloRequest> helloRequestList = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            helloRequestList.add(
                HelloRequest.newBuilder()
                    .setName(i + ". " + "rotomoo")
                    .setAge(32 + i)
                    .setMessage(i + ". " + "client -> server Request")
                    .build()
            );
        }

        for (HelloRequest helloRequest: helloRequestList) {
            requestObserver.onNext(helloRequest);
        }
        requestObserver.onCompleted();

        try {
            // 응답 대기
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 서버에 Bidirectional Streaming RPC
     */
    public void sendBidirectionalStreamingAsync() {
        // 비동기 방식이기 때문에 서버 Response 를 기다릴 CountDownLatch 추가
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<HelloResponse> responseObserver = new StreamObserver<HelloResponse>() {
            @Override
            public void onNext(HelloResponse helloResponse) {
                logger.info("\n=== Bidirectional Streaming Response Data \n{}===", helloResponse);
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<HelloRequest> requestObserver = asyncStub.bidiHello(responseObserver);

        List<HelloRequest> helloRequestList = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            helloRequestList.add(
                HelloRequest.newBuilder()
                    .setName(i + ". " + "rotomoo")
                    .setAge(32 + i)
                    .setMessage(i + ". " + "client -> server Request")
                    .build()
            );
        }

        for (HelloRequest helloRequest: helloRequestList) {
            requestObserver.onNext(helloRequest);
        }
        requestObserver.onCompleted();

        try {
            // 응답 대기
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
