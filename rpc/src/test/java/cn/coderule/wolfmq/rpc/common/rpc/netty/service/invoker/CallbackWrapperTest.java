package cn.coderule.wolfmq.rpc.common.rpc.netty.service.invoker;

import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.ResponseFuture;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCallback;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CallbackWrapperTest {

    private AddressInvoker invoker;
    private RpcCallback rpcCallback;
    private CallbackWrapper wrapper;

    @BeforeEach
    void setUp() {
        invoker = mock(AddressInvoker.class);
        rpcCallback = mock(RpcCallback.class);
        wrapper = new CallbackWrapper(invoker, rpcCallback, "127.0.0.1:10911");
    }

    @Test
    void constructor_ShouldSetFields() {
        assertNotNull(wrapper);
    }

    @Test
    void onSuccess_ShouldDelegateAndUpdateTime() {
        RpcCommand response = RpcCommand.createResponseCommand(200, "OK");
        wrapper.onSuccess(response);

        verify(invoker).updateLastResponseTime("127.0.0.1:10911");
        verify(rpcCallback).onSuccess(response);
    }

    @Test
    void onComplete_ShouldDelegateToCallback() {
        ResponseFuture future = mock(ResponseFuture.class);
        wrapper.onComplete(future);
        verify(rpcCallback).onComplete(future);
    }

    @Test
    void onFailure_ShouldDelegateToCallback() {
        Throwable t = new RuntimeException("test error");
        wrapper.onFailure(t);
        verify(rpcCallback).onFailure(t);
    }
}