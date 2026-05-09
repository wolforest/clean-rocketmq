package cn.coderule.wolfmq.rpc.common.rpc.core.invoke;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RpcCallbackTest {

    @Test
    void testOnSuccess() {
        RpcCommand response = RpcCommand.createResponseCommand(0, "OK");
        
        RpcCallback callback = new RpcCallback() {
            @Override
            public void onSuccess(RpcCommand response) {
                assertNotNull(response);
                assertEquals(0, response.getCode());
            }

            @Override
            public void onFailure(Throwable e) {
                fail("Should not fail");
            }

            @Override
            public void onComplete(ResponseFuture future) {
                assertNotNull(future);
            }
        };
        
        callback.onSuccess(response);
    }

    @Test
    void testOnFailure() {
        Exception exception = new RuntimeException("Test error");
        
        RpcCallback callback = new RpcCallback() {
            @Override
            public void onSuccess(RpcCommand response) {
                fail("Should not succeed");
            }

            @Override
            public void onFailure(Throwable e) {
                assertNotNull(e);
                assertEquals("Test error", e.getMessage());
            }

            @Override
            public void onComplete(ResponseFuture future) {
                assertNotNull(future);
            }
        };
        
        callback.onFailure(exception);
    }

    @Test
    void testOnComplete() {
        ResponseFuture future = new ResponseFuture(null, new RpcCommand(), 1000L);
        
        RpcCallback callback = new RpcCallback() {
            @Override
            public void onSuccess(RpcCommand response) {
            }

            @Override
            public void onFailure(Throwable e) {
            }

            @Override
            public void onComplete(ResponseFuture future) {
                assertNotNull(future);
            }
        };
        
        callback.onComplete(future);
    }
}
