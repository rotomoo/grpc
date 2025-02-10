# RPC (Remote Procedure Call)

리모트 프로시저 콜(Remote Procedure Call, RPC)은 네트워크로 연결된 다른 프로그램에 있는 함수나 프로시저를 호출하고, 그 결과를 응답받는 방식이다.

프로그램마다 언어의 데이터 타입, 메서드 호출 등이 다를 수 있기 때문에, 이를 표준화하기 위해 명시된 인터페이스 정의 언어(IDL, Interface Description Language)를 사용하여 **Stub코드**를 생성한다.

<br>

## IDL (Interface Description Language)

서로 다른 언어와 환경을 사용하는 컴포넌트 사이의 통신을 가능하게 한다.

예시로 프로토콜 버퍼(Protobuf, Protocol Buffers), 아파치 쓰리프트(Apache Thrift) 등이 있다.

또하나의 예시로 Open API의 명세인 Swagger가 REST API의 IDL이다.

여기서 XML, JSON 자체로는 IDL이 아닌 데이터 표현 방식이다.

<br>

**Protobuf IDL**

```
message HelloRequest {
  string name = 1;      // 이름
  int32 age = 2;        // 나이
  string message = 3;   // 하고 싶은 말 메시지
}

message HelloResponse {
  string greeting_message = 1;  // 인사 메시지
  string question_message = 2;  // 질문 메시지
}

service Hello {
  rpc SayHello (HelloRequest) returns (HelloResponse);
  rpc LotsOfReplies(HelloRequest) returns (stream HelloResponse);
  rpc LotsOfGreetings(stream HelloRequest) returns (HelloResponse);
  rpc BidiHello(stream HelloRequest) returns (stream HelloResponse);
}
```

<br>

**Apache Thrift IDL**

```
enum PhoneType {
  HOME,
  WORK,
  MOBILE,
  OTHER
}

struct Phone {
  1: i32 id,
  2: string number,
  3: PhoneType type
}

service PhoneSvc {
  Phone findById(1: i32 id),
  list<Phone> findAll()
}
```

이 IDL들이 어떻게 사용되는지는 GRPC에서 알아보자.

<br>

## Stub

Stub은 RPC의 핵심 개념이다.

서버와 클라이언트는 서로 다른 주소공간을 사용하므로, 함수 호출에 사용된 매개변수를 변환해야 된다.

이때 Stub을 사용하여 매개변수를 변환 한다.

- **Client stub:** 함수 호출에 사용된 파라미터의 변환(Marshalling) 및 함수 실행 후 서버에 전달된 결과 반환을 담당한다.
- **Server stub:** 클라이언트 요청시 전달한 매개변수의 역변환(Unmarshalling) 및 함수 실행 결과 변환을 담당한다.

마찬가지로 이 Stub이 어떻게 사용되는지는 GRPC 에서 알아보자.

<br>

# GRPC

GRPC는 구글에서 개발한 오픈 소스 RPC 프레임워크이다.

프로그램간 통신으로 Protobuf를 사용한다.

<br>

## **GRPC 통신 종류**

(참고 : [grpc 통신 종류](https://grpc.io/docs/what-is-grpc/core-concepts/))

![image](https://github.com/user-attachments/assets/0562fea2-2e86-405f-a3f2-d852a82648bb)

<br>

**1. Unary RPC (단항 RPC)**

```java
// 1.Unary RPC (단항 RPC)
rpc SayHello(HelloRequest) returns (HelloResponse);
```

- 요청 1개당 응답 1개
- 기존 API와 동일한 방식이다.

<br>

**2. Server Streaming RPC (서버 스트리밍 RPC)**

```java
// 2. Server Streaming RPC (서버 스트리밍 RPC)
rpc LotsOfReplies(HelloRequest) returns (stream HelloResponse);
```

- 요청 1개당 응답 n개
- HTTP/2.0의 스트리밍 기능을 활용한 방식
- 클라이언트가 한 번 요청하면, 서버가 여러 개의 응답을 순차적으로 전송한다.
  
<br>

**3. Client Streaming RPC (클라이언트 스트리밍 RPC)**

```java
// 3. Client Streaming RPC (클라이언트 스트리밍 RPC)
rpc LotsOfGreetings(stream HelloRequest) returns (HelloResponse);
```

- 요청 n개당 응답 1개
- HTTP/2.0의 스트리밍 기능을 활용한 방식
- 클라이언트가 여러 개의 요청을 보낸 후, 서버가 한 번만 응답
- 응답 시점은 서버가 결정한다.
  
<br>

**4. Bidirectional Streaming RPC (양방향 스트리밍 RPC)**

```java
// 4. Bidirectional Streaming RPC (양방향 스트리밍 RPC)
rpc BidiHello(stream HelloRequest) returns (stream HelloResponse);
```

- 요청 n개, 응답 n개
- 클라이언트와 서버가 각각 독립적인 스트림(Stream)을 유지하며 양방향 통신
- 순서에 관계없이 데이터를 읽고 쓸 수 있다.
  
<br>

## 구현

위의 4가지 통신을 자바로 구현해보고 IDL, Stub이 어떻게 사용되는지 알아보자.
  
<br>

### **build.gradle**

```groovy
dependencies {
    // grpc
    runtimeOnly 'io.grpc:grpc-netty-shaded:1.70.0'
    implementation 'io.grpc:grpc-protobuf:1.70.0'
    implementation 'io.grpc:grpc-stub:1.70.0'
    compileOnly 'org.apache.tomcat:annotations-api:6.0.53' // necessary for Java 9+
}
```

GRPC 의존성을 추가해준다.
  
<br>

### **IDL 작성**

```protobuf
// ref : https://herojoon-dev.tistory.com/217

syntax = "proto3";

// 생성될 class N개 여부
option java_multiple_files = true;

// 생성될 class 패키지명
option java_package = "com.example.proto";

message HelloRequest {
  string name = 1;      // 이름
  int32 age = 2;        // 나이
  string message = 3;   // 하고 싶은 말 메시지
}

message HelloResponse {
  string greeting_message = 1;  // 인사 메시지
  string question_message = 2;  // 질문 메시지
}

service Hello {
  rpc SayHello (HelloRequest) returns (HelloResponse);  // Unary RPC (단방향 RPC)
  rpc LotsOfReplies(HelloRequest) returns (stream HelloResponse);  // Server streaming RPC (서버 스트리밍 RPC)
  rpc LotsOfGreetings(stream HelloRequest) returns (HelloResponse);  // Client streaming RPC (클라이언트 스트리밍 RPC)
  rpc BidiHello(stream HelloRequest) returns (stream HelloResponse);  // Bidirectional streaming RPC (양방향 스트리밍 RPC)
}
```

.proto 파일을 작성한다.
  
<br>

### Stub 생성

<img width="354" alt="image (1)" src="https://github.com/user-attachments/assets/bfda82d0-0fe3-48f6-89f0-4df9543df500" />

./gradlew build 또는 generateProto 를 통해 빌드 한다.

<img width="368" alt="image (2)" src="https://github.com/user-attachments/assets/5997df53-6194-4509-abbe-b177a61d57e0" />

build 디렉터리에 GRPC 관련 클래스들이 만들어진다.

HelloGrpc 클래스를 조금 훑어보자.

```java
/**
 * Creates a new blocking-style stub that supports unary and streaming output calls on the service
 */
public static HelloBlockingStub newBlockingStub(io.grpc.Channel channel) {
  io.grpc.stub.AbstractStub.StubFactory<HelloBlockingStub> factory =
    new io.grpc.stub.AbstractStub.StubFactory<HelloBlockingStub>() {
      @java.lang.Override
      public HelloBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
        return new HelloBlockingStub(channel, callOptions);
      }
    };
  return HelloBlockingStub.newStub(factory, channel);
}
```

channel을 매개변수로 받아 HelloBlockingStub을 반환하는 메서드이다.

**동기(synchronous) 방식**으로 gRPC 호출을 수행한다.

즉, 클라이언트가 요청을 보내고 응답을 받을 때까지 **블로킹**되며

단항(Unary), 서버 스트리밍(Server Streaming) RPC에 사용한다.
  
<br>

```java
/**
 * Creates a new async stub that supports all call types for the service
 */
public static HelloStub newStub(io.grpc.Channel channel) {
  io.grpc.stub.AbstractStub.StubFactory<HelloStub> factory =
    new io.grpc.stub.AbstractStub.StubFactory<HelloStub>() {
      @java.lang.Override
      public HelloStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
        return new HelloStub(channel, callOptions);
      }
    };
  return HelloStub.newStub(factory, channel);
}
```

똑같이 channel을 매개변수로 받아 HelloStub을 반환하는 메서드이다.

**비동기(Asynchronous) 방식**으로 gRPC 호출을 수행한다.

StreamObserver를 사용해 응답을 받으며,

클라이언트 스트리밍(Client Streaming), 바이다이렉셔날 스트리밍(Bidirectional Streaming) 에 사용한다.
  
<br>

### Channel

GRPC 호출에 필요한 매개변수 Channel을 만들어보자.

```java
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

```

먼저 서버를 가동한다.

awaitTermination()을 통해 서버가 종료되지 않게 한다.
  
<br>

```java
public class HelloGrpcClient {

    public static void main(String[] args) {
        // 스텁(Stub)에 대한 gRPC 채널 생성 및 연결 서버 주소와 포트 지정
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 8080)
            .usePlaintext()
            .build();

        HelloGrpcClientCaller helloGrpcClientCaller = new HelloGrpcClientCaller(channel);
        helloGrpcClientCaller.sendUnaryBlocking();
    }
}

```

클라이언트에 GRPC 채널을 생성해 서버와 연결해준다.
  
<br>

### 클라이언트 → 서버 요청

```java
/**
 * 클라이언트 동작 코드 (서버에 요청)
 */
public class HelloGrpcClientCaller {

    private ManagedChannel channel;
    private HelloGrpc.HelloBlockingStub blockingStub;
    private static final Logger logger = LoggerFactory.getLogger(HelloGrpcClientCaller.class);

    public HelloGrpcClientCaller(ManagedChannel chl) {
        channel = chl;
        blockingStub = HelloGrpc.newBlockingStub(channel);
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
```

단항(Unary RPC)는 **동기(synchronous) 방식**으로 newBlockingStub 메서드를 사용한다.
  
<br>

```protobuf
message HelloRequest {
  string name = 1;      // 이름
  int32 age = 2;        // 나이
  string message = 3;   // 하고 싶은 말 메시지
}

service Hello {
  rpc SayHello (HelloRequest) returns (HelloResponse);  // Unary RPC (단방향 RPC)
}
```
IDL로 미리 정의해두었던 .proto 파일의 스펙에 맞게 클라이언트 → 서버로 요청한다.

<br>

```java
/**
 * <pre>
 * Unary RPC (단방향 RPC)
 * </pre>
 */
public com.example.proto.HelloResponse sayHello(com.example.proto.HelloRequest request) {
  return io.grpc.stub.ClientCalls.blockingUnaryCall(
      getChannel(), getSayHelloMethod(), getCallOptions(), request);
}
    
    
public static <ReqT, RespT> RespT blockingUnaryCall(
    Channel channel, MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, ReqT req) {
  ThreadlessExecutor executor = new ThreadlessExecutor();
  boolean interrupt = false;
  ClientCall<ReqT, RespT> call = channel.newCall(method,
      callOptions.withOption(ClientCalls.STUB_TYPE_OPTION, StubType.BLOCKING)
          .withExecutor(executor));
  try {
    ListenableFuture<RespT> responseFuture = futureUnaryCall(call, req);
    while (!responseFuture.isDone()) {
      try {
        executor.waitAndDrain();
      } catch (InterruptedException e) {
        interrupt = true;
        call.cancel("Thread interrupted", e);
        // Now wait for onClose() to be called, so interceptors can clean up
      }
    }
    executor.shutdown();
    return getUnchecked(responseFuture);
  } catch (RuntimeException | Error e) {
    // Something very bad happened. All bets are off; it may be dangerous to wait for onClose().
    throw cancelThrow(call, e);
  } finally {
    if (interrupt) {
      Thread.currentThread().interrupt();
    }
  }
}
```

그 요청은 GRPC 빌드를 통해 만들어지며 스펙에 맞게 채널을 통해 서버로 호출하는것을 알 수 있다.
  
<br>

### 서버 → 클라이언트 응답

```java
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

        // response 데이터 셋업
        HelloResponse helloResponse = HelloResponse.newBuilder()
            .setGreetingMessage("hi, " + helloRequest.getName() + " age:" + helloRequest.getAge())  // .proto에 정의한 response value
            .setQuestionMessage("server -> client Response arrived?")  // .proto에 정의한 response value
            .build();

        responseObserver.onNext(helloResponse);
        responseObserver.onCompleted();
    }
```

서버 또한 동일하게 GRPC 빌드를 통해 만들어진다. (생략)

그 메서드를 재정의(Override) 하여 **StreamObserver 객체를** 통해 클라이언트에서 응답을 받을 수 있다.
  
<br>

### 로그

이제 애플리케이션 로그를 확인해보자.

위 코드들의 logger.info() 부분들을 자세히 보면

**Server**

```java
2025-02-10 18:09:27.122 [grpc-default-executor-0] INFO  c.e.grpc.server.HelloGrpcServiceImpl -
    === Unary Request Data
    name: "rotomoo"
    age: 32
    message: "client -> server Request"
    ===
```

클라이언트 → 서버로 요청을 보낼때 만든 요청 데이터가 서버 로그에서도 조회되며,
  
<br>

**Client**

```java
2025-02-10 18:09:27.176 [main] INFO  c.e.g.client.HelloGrpcClientCaller -
    === Unary Response Data
    greeting_message: "hi, rotomoo age:32"
    question_message: "server -> client Response arrived?"
    ===

```

서버 → 클라이언트로 응답할때 보낸 응답 데이터가 클라이언트 로그에서도 조회되는걸 볼 수 있다.
  
<br>

## 결론

<img width="675" alt="image (3)" src="https://github.com/user-attachments/assets/d96ed0e6-37b3-4c44-8d7f-4b18cff78c0f" />

IDL로 정의한 Stub을 생성하여 GRPC Channel을 통해 클라이언트→서버로 호출하여 요청, 응답을 주고 받는다.
  
<br>

# ref

[grpc-java github](https://github.com/grpc/grpc-java/tree/master)

[https://ankeetmaini.dev/posts/grpc-services/](https://ankeetmaini.dev/posts/grpc-services/)

[https://www.geeksforgeeks.org/remote-procedure-call-rpc-in-operating-system/](https://www.geeksforgeeks.org/remote-procedure-call-rpc-in-operating-system/)
