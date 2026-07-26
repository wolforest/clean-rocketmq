package cn.coderule.wolfmq.broker.server.grpc.service.channel;

import cn.coderule.wolfmq.rpc.common.grpc.RequestPipeline;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelemetryObserverTest {

    @Test
    void constructor_ShouldCreateObserver() {
        RequestPipeline pipeline = (ctx, headers, request) -> {};
        TelemetryService telemetryService = mock(TelemetryService.class);
        when(telemetryService.telemetry(any())).thenReturn(mock(cn.coderule.wolfmq.rpc.broker.grpc.ContextStreamObserver.class));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        TelemetryObserver observer = new TelemetryObserver(
            pipeline,
            executor,
            telemetryService,
            mock(StreamObserver.class)
        );
        assertNotNull(observer);
        executor.shutdownNow();
    }
}
