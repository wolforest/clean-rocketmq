package cn.coderule.wolfmq.rpc.common.grpc;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import io.grpc.Metadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestPipelineTest {

    @Test
    void execute_ShouldCallPipeline() {
        boolean[] called = {false};
        RequestPipeline pipeline = (ctx, headers, request) -> called[0] = true;

        pipeline.execute(mock(RequestContext.class), mock(Metadata.class), null);
        assertTrue(called[0]);
    }

    @Test
    void pipe_ShouldChainTwo() {
        boolean[] called1 = {false};
        boolean[] called2 = {false};
        RequestPipeline pipeline1 = (ctx, headers, request) -> called1[0] = true;
        RequestPipeline pipeline2 = (ctx, headers, request) -> called2[0] = true;

        RequestPipeline chained = pipeline1.pipe(pipeline2);
        chained.execute(mock(RequestContext.class), mock(Metadata.class), null);

        assertTrue(called1[0]);
        assertTrue(called2[0]);
    }

    @Test
    void pipe_ShouldReturnNonNull() {
        RequestPipeline pipeline1 = (ctx, headers, request) -> {};
        RequestPipeline pipeline2 = (ctx, headers, request) -> {};

        RequestPipeline chained = pipeline1.pipe(pipeline2);
        assertNotNull(chained);
    }
}