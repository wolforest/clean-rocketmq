package com.wolfmq.broker.server.grpc.service.channel;

import com.wolfmq.broker.server.grpc.interceptor.PipelineExecutor;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelemetryObserverTest {
    @Mock
    private PipelineExecutor pipelineExecutor;

    @Test
    void constructor_ShouldCreateObserver() {
        TelemetryService telemetryService = mock(TelemetryService.class);
        when(telemetryService.telemetry(any())).thenReturn(mock(com.wolfmq.rpc.broker.grpc.ContextStreamObserver.class));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        TelemetryObserver observer = new TelemetryObserver(
            pipelineExecutor,
            executor,
            telemetryService,
            mock(StreamObserver.class)
        );
        assertNotNull(observer);
        executor.shutdownNow();
    }
}
