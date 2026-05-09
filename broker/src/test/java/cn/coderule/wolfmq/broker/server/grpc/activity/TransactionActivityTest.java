package cn.coderule.wolfmq.broker.server.grpc.activity;

import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.EndTransactionResponse;
import cn.coderule.wolfmq.broker.server.grpc.service.TransactionService;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionActivityTest {

    @Mock
    private ThreadPoolExecutor executor;

    @Mock
    private TransactionService transactionService;

    @Mock
    private StreamObserver<EndTransactionResponse> responseObserver;

    private TransactionActivity activity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        activity = new TransactionActivity(executor);
        activity.inject(transactionService);
    }

    @Test
    void testConstructor() {
        assertNotNull(activity);
    }

    @Test
    void testInject_setsTransactionService() {
        TransactionActivity newActivity = new TransactionActivity(executor);
        newActivity.inject(transactionService);
        assertNotNull(newActivity);
    }

    @Test
    void testSubmit_withValidRequest_submitsToExecutor() {
        RequestContext context = RequestContext.builder().build();
        EndTransactionRequest request = EndTransactionRequest.newBuilder().build();
        CompletableFuture<EndTransactionResponse> future = new CompletableFuture<>();

        when(transactionService.submit(any(), any())).thenReturn(future);
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        activity.submit(context, request, responseObserver);

        verify(transactionService).submit(any(), any());
        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testSubmit_serviceThrowsException_writesError() {
        RequestContext context = RequestContext.builder().build();
        EndTransactionRequest request = EndTransactionRequest.newBuilder().build();

        when(transactionService.submit(any(), any()))
            .thenThrow(new RuntimeException("service error"));
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        activity.submit(context, request, responseObserver);

        verify(transactionService).submit(any(), any());
    }

    @Test
    void testSubmit_executorThrowsException_writesError() {
        RequestContext context = RequestContext.builder().build();
        EndTransactionRequest request = EndTransactionRequest.newBuilder().build();

        when(executor.submit(any(Runnable.class)))
            .thenThrow(new RuntimeException("executor error"));

        assertDoesNotThrow(() -> activity.submit(context, request, responseObserver));
    }

    @Test
    void testSubmit_withNullTransactionService_doesNotThrow() {
        TransactionActivity newActivity = new TransactionActivity(executor);
        RequestContext context = RequestContext.builder().build();
        EndTransactionRequest request = EndTransactionRequest.newBuilder().build();

        assertDoesNotThrow(() -> newActivity.submit(context, request, responseObserver));
    }
}