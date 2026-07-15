package cn.coderule.wolfmq.rpc.common.grpc.response;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseWriterTest {

    @Test
    void getInstance_ShouldReturnSameInstance() {
        ResponseWriter instance1 = ResponseWriter.getInstance();
        ResponseWriter instance2 = ResponseWriter.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    void writeResponse_ShouldCallOnNext() {
        StreamObserver<String> observer = mock(StreamObserver.class);
        ResponseWriter writer = ResponseWriter.getInstance();

        boolean result = writer.writeResponse(observer, "test-response");

        assertTrue(result);
        verify(observer).onNext("test-response");
    }

    @Test
    void write_ShouldCallOnNextAndOnCompleted() {
        StreamObserver<String> observer = mock(StreamObserver.class);
        ResponseWriter writer = ResponseWriter.getInstance();

        writer.write(observer, "test-response");

        verify(observer).onNext("test-response");
        verify(observer).onCompleted();
    }

    @Test
    void writeResponse_WithNullResponse_ShouldNotThrow() {
        StreamObserver<String> observer = mock(StreamObserver.class);
        ResponseWriter writer = ResponseWriter.getInstance();

        boolean result = writer.writeResponse(observer, null);

        assertFalse(result);
    }
}