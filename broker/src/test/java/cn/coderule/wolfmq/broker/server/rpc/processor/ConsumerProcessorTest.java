package cn.coderule.wolfmq.broker.server.rpc.processor;

import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumerProcessorTest {

    @Test
    void process_ShouldReturnNull() throws Exception {
        ConsumerProcessor processor = new ConsumerProcessor();
        RpcContext ctx = mock(RpcContext.class);
        RpcCommand request = mock(RpcCommand.class);
        RpcCommand result = processor.process(ctx, request);
        assertNull(result);
    }

    @Test
    void reject_ShouldReturnFalse() {
        ConsumerProcessor processor = new ConsumerProcessor();
        assertFalse(processor.reject());
    }
}
