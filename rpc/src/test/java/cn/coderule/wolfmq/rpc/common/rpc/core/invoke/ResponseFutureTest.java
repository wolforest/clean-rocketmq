package cn.coderule.wolfmq.rpc.common.rpc.core.invoke;

import cn.coderule.common.lang.concurrent.sync.SemaphoreGuard;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingException;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingSendRequestException;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingTimeoutException;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.code.ResponseCode;
import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ResponseFutureTest {

    @Test
    void testConstructorMinimal() {
        Channel channel = mock(Channel.class);
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        long timeout = 3000L;
        
        ResponseFuture future = new ResponseFuture(channel, request, timeout);
        
        assertNotNull(future);
        assertSame(channel, future.getChannel());
        assertEquals(1, future.getOpaque());
        assertSame(request, future.getRequest());
        assertEquals(timeout, future.getTimeoutMillis());
        assertNull(future.getInvokeCallback());
        assertNull(future.getSemaphoreGuard());
        assertTrue(future.isSendRequestOK());
    }

    @Test
    void testConstructorFull() {
        Channel channel = mock(Channel.class);
        int opaque = 123;
        long timeout = 5000L;
        RpcCallback callback = mock(RpcCallback.class);
        SemaphoreGuard guard = mock(SemaphoreGuard.class);
        RpcCommand request = new RpcCommand();
        
        ResponseFuture future = new ResponseFuture(channel, opaque, request, timeout, callback, guard);
        
        assertNotNull(future);
        assertSame(channel, future.getChannel());
        assertEquals(opaque, future.getOpaque());
        assertSame(request, future.getRequest());
        assertEquals(timeout, future.getTimeoutMillis());
        assertSame(callback, future.getInvokeCallback());
        assertSame(guard, future.getSemaphoreGuard());
    }

    @Test
    void testPutAndWaitResponse() throws InterruptedException {
        Channel channel = mock(Channel.class);
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        ResponseFuture future = new ResponseFuture(channel, request, 3000L);
        
        RpcCommand response = RpcCommand.createResponseCommand(ResponseCode.SUCCESS, "OK");
        
        future.putResponse(response);
        RpcCommand result = future.waitResponse(1000);
        
        assertSame(response, result);
    }

    @Test
    void testWaitResponseTimeout() throws InterruptedException {
        Channel channel = mock(Channel.class);
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        ResponseFuture future = new ResponseFuture(channel, request, 3000L);
        
        RpcCommand result = future.waitResponse(100);
        
        assertNull(result);
    }

    @Test
    void testIsTimeout() throws InterruptedException {
        Channel channel = mock(Channel.class);
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        ResponseFuture future = new ResponseFuture(channel, request, 1L);
        
        Thread.sleep(10);
        
        assertTrue(future.isTimeout());
    }

    @Test
    void testIsNotTimeout() {
        Channel channel = mock(Channel.class);
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        ResponseFuture future = new ResponseFuture(channel, request, 10000L);
        
        assertFalse(future.isTimeout());
    }

    @Test
    void testRelease() {
        Channel channel = mock(Channel.class);
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        SemaphoreGuard guard = mock(SemaphoreGuard.class);
        ResponseFuture future = new ResponseFuture(channel, 1, request, 3000L, null, guard);
        
        future.release();
        
        verify(guard).release();
    }

    @Test
    void testReleaseWithNullGuard() {
        Channel channel = mock(Channel.class);
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        ResponseFuture future = new ResponseFuture(channel, request, 3000L);
        
        assertDoesNotThrow(() -> future.release());
    }

    @Test
    void testExecuteRpcCallbackWithSuccess() {
        Channel channel = mock(Channel.class);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        RpcCallback callback = mock(RpcCallback.class);
        RpcCommand response = RpcCommand.createResponseCommand(ResponseCode.SUCCESS, "OK");
        
        ResponseFuture future = new ResponseFuture(channel, 1, request, 3000L, callback, null);
        future.putResponse(response);
        
        future.executeRpcCallback();
        
        verify(callback).onSuccess(response);
        verify(callback).onComplete(future);
    }

    @Test
    void testExecuteRpcCallbackWithSendFailure() {
        Channel channel = mock(Channel.class);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        RpcCallback callback = mock(RpcCallback.class);
        
        ResponseFuture future = new ResponseFuture(channel, 1, request, 3000L, callback, null);
        future.setSendRequestOK(false);
        future.setCause(new RuntimeException("Send failed"));
        
        future.executeRpcCallback();
        
        verify(callback).onFailure(any(RemotingSendRequestException.class));
        verify(callback).onComplete(future);
    }

    @Test
    void testExecuteRpcCallbackWithTimeout() {
        Channel channel = mock(Channel.class);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        RpcCallback callback = mock(RpcCallback.class);
        
        ResponseFuture future = new ResponseFuture(channel, 1, request, 1L, callback, null);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        future.executeRpcCallback();
        
        verify(callback).onFailure(any(RemotingTimeoutException.class));
        verify(callback).onComplete(future);
    }

    @Test
    void testExecuteRpcCallbackWithNullCallback() {
        Channel channel = mock(Channel.class);
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        
        ResponseFuture future = new ResponseFuture(channel, request, 3000L);
        
        assertDoesNotThrow(() -> future.executeRpcCallback());
    }

    @Test
    void testExecuteRpcCallbackOnlyOnce() {
        Channel channel = mock(Channel.class);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        RpcCallback callback = mock(RpcCallback.class);
        RpcCommand response = RpcCommand.createResponseCommand(ResponseCode.SUCCESS, "OK");
        
        ResponseFuture future = new ResponseFuture(channel, 1, request, 3000L, callback, null);
        future.putResponse(response);
        
        future.executeRpcCallback();
        future.executeRpcCallback();
        
        verify(callback, times(1)).onSuccess(response);
        verify(callback, times(1)).onComplete(future);
    }

    @Test
    void testInterrupt() {
        Channel channel = mock(Channel.class);
        when(channel.remoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        RpcCallback callback = mock(RpcCallback.class);
        
        ResponseFuture future = new ResponseFuture(channel, 1, request, 3000L, callback, null);
        
        future.interrupt();
        
        assertTrue(future.isInterrupted());
        verify(callback).onFailure(any(RemotingException.class));
        verify(callback).onComplete(future);
    }

    @Test
    void testToString() {
        Channel channel = mock(Channel.class);
        RpcCommand request = new RpcCommand();
        request.setOpaque(1);
        
        ResponseFuture future = new ResponseFuture(channel, request, 3000L);
        
        String str = future.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("opaque=1"));
        assertTrue(str.contains("sendRequestOK=true"));
    }
}
