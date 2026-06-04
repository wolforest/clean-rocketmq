package cn.coderule.wolfmq.rpc.common.grpc.activity;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.rpc.common.grpc.core.GrpcTask;
import apache.rocketmq.v2.Status;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RejectActivityTest {

    @Mock
    private StreamObserver<Status> responseObserver;

    @Mock
    private Runnable regularRunnable;

    private RejectActivity rejectActivity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rejectActivity = new RejectActivity();
    }

    @Test
    void testRejectedExecution_withNonGrpcTask_doesNothing() {
        ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);

        rejectActivity.rejectedExecution(regularRunnable, executor);

        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }

    @Test
    void testRejectedExecution_withGrpcTask_writesResponse() {
        RequestContext context = RequestContext.builder().build();
        Status request = Status.newBuilder().build();
        Status errorResponse = Status.newBuilder().build();

        GrpcTask<Status, Status> grpcTask = new GrpcTask(
            () -> {},
            context,
            request,
            responseObserver,
            errorResponse
        );

        ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
        rejectActivity.rejectedExecution(grpcTask, executor);

        verify(responseObserver).onNext(errorResponse);
        verify(responseObserver).onCompleted();
    }
}