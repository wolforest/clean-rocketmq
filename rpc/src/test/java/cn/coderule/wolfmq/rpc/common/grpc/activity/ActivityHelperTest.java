package cn.coderule.wolfmq.rpc.common.grpc.activity;

import apache.rocketmq.v2.Status;
import cn.coderule.wolfmq.rpc.common.grpc.core.GrpcTask;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActivityHelperTest {

    @Mock
    private StreamObserver<Status> responseObserver;

    private Function<Status, Status> statusToResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        statusToResponse = status -> status;
    }

    @Test
    void testConstructor_withContextAndRequest() {
        Status request = Status.newBuilder().build();
        ActivityHelper<Status, Status> helper = new ActivityHelper<>(request, responseObserver);
        assertNotNull(helper);
    }

    @Test
    void testCreateTask_createsGrpcTask() {
        Status request = Status.newBuilder().build();
        ActivityHelper<Status, Status> helper = new ActivityHelper<>(null, request, responseObserver, statusToResponse);

        Runnable runnable = () -> {};
        GrpcTask<Status, Status> task = helper.createTask(runnable);

        assertNotNull(task);
        assertEquals(runnable, task.getRunnable());
        assertEquals(request, task.getRequest());
    }

    @Test
    void testWriteResponse_withNullThrowable_callsWriter() {
        Status request = Status.newBuilder().build();
        Status response = Status.newBuilder().build();
        ActivityHelper<Status, Status> helper = new ActivityHelper<>(null, request, responseObserver, statusToResponse);

        helper.writeResponse(response, null);

        verify(responseObserver).onNext(response);
        verify(responseObserver).onCompleted();
    }

    @Test
    void testWriteResponse_withThrowable_writesErrorResponse() {
        Status request = Status.newBuilder().build();
        ActivityHelper<Status, Status> helper = new ActivityHelper<>(null, request, responseObserver, statusToResponse);

        RuntimeException exception = new RuntimeException("test error");
        helper.writeResponse(null, exception);

        verify(responseObserver, never()).onError(any());
        verify(responseObserver).onNext(any(Status.class));
        verify(responseObserver).onCompleted();
    }
}