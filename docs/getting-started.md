# Getting-Started

## Setup
x-remoting JARs are available via Maven Central. If you are using Maven, just add the following lines to  your pom.xml:
```xml
<dependency>
    <groupId>io.github.x-infra-lab</groupId>
    <artifactId>x-remoting</artifactId>
    <version>${version}</version>
</dependency>
```

Maven Central lastest version :  [![Maven Central](https://img.shields.io/maven-central/v/io.github.x-infra-lab/x-remoting)](https://central.sonatype.com/artifact/io.github.x-infra-lab/x-remoting/)
## Remoting Call
* define request message class
```java
public class SimpleRequest implements Serializable {

	private String msg;

	public SimpleRequest() {
	}

	public SimpleRequest(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}

```
* define a UserProcessor
```java
public class SimpleUserProcessor implements UserProcessor<SimpleRequest> {

	@Override
	public String interest() {
		return SimpleRequest.class.getName();
	}

	@Override
	public Object handRequest(SimpleRequest request) {
		return "echo:" + request.getMsg();
	}

}
```
* start a RpcServer
```java
RpcServer rpcServer = new RpcServer();
rpcServer.startup();

rpcServer.registerUserProcessor(new SimpleUserProcessor());
```
* start a RpcClient
```java
RpcClient rpcClient = new RpcClient();
rpcClient.startup();
```
### SyncCall
```java
@Test
public void testSyncCall() throws RemotingException, InterruptedException {
    String msg = "hello x-remoting";
    SimpleRequest request = new SimpleRequest(msg);
    String result = rpcClient.syncCall(request, rpcServer.localAddress(), 1000);
    
    Assertions.assertEquals(result, "echo:" + msg);
}
```
### AsyncCall - Future
```java
@Test
public void testAsyncCall1() throws RemotingException, InterruptedException, TimeoutException {
    String msg = "hello x-remoting";
    SimpleRequest request = new SimpleRequest(msg);
    RpcInvokeFuture<String> future = rpcClient.asyncCall(request, rpcServer.localAddress(), 1000);

    String result = future.get(3, TimeUnit.SECONDS);
    Assertions.assertEquals(result, "echo:" + msg);
}
```
### AsyncCall - Callback
```java
@Test
public void testAsyncCall2() throws RemotingException, InterruptedException, TimeoutException {
    String msg = "hello x-remoting";
    SimpleRequest request = new SimpleRequest(msg);

    CountDownLatch countDownLatch = new CountDownLatch(1);
    AtomicReference<String> result = new AtomicReference<>();
    rpcClient.asyncCall(request, rpcServer.localAddress(), 1000, new RpcInvokeCallBack<String>() {
        @Override
        public void onException(Throwable t) {
            countDownLatch.countDown();
        }

        @Override
        public void onResponse(String response) {
            result.set(response);
            countDownLatch.countDown();
        }
    });

    countDownLatch.await(3, TimeUnit.SECONDS);
    Assertions.assertEquals(result.get(), "echo:" + msg);
}
```
### OnewayCall
```java
@Test
public void testOnewayCall() throws RemotingException, InterruptedException {
    String msg = "hello x-remoting";
    SimpleRequest request = new SimpleRequest(msg);

    rpcClient.oneway(request, rpcServer.localAddress());
    TimeUnit.SECONDS.sleep(2);
}
```
