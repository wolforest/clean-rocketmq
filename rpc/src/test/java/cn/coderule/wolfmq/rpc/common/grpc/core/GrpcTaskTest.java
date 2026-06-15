package cn.coderule.wolfmq.rpc.common.grpc.core;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrpcTaskTest {

    @Test
    void testRun() {
        Runnable runnable = mock(Runnable.class);
        RequestContext context = RequestContext.builder().build();
        String request = "req";
        StreamObserver<String> observer = mock(StreamObserver.class);
        String rejectResponse = "rejected";

        GrpcTask<String, String> task = new GrpcTask<>(runnable, context, request, observer, rejectResponse);

        assertEquals(context, task.getContext());
        assertEquals(request, task.getRequest());
        assertEquals(observer, task.getStreamObserver());
        assertEquals(rejectResponse, task.getExecuteRejectResponse());

        task.run();
        verify(runnable).run();
    }
}