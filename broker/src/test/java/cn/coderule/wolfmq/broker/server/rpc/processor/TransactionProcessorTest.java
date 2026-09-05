package cn.coderule.wolfmq.broker.server.rpc.processor;

import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionProcessorTest {

    @Test
    void process_ShouldReturnNull() throws Exception {
        TransactionProcessor processor = new TransactionProcessor();
        RpcContext ctx = mock(RpcContext.class);
        RpcCommand request = mock(RpcCommand.class);
        RpcCommand result = processor.process(ctx, request);
        assertNull(result);
    }

    @Test
    void reject_ShouldReturnFalse() {
        TransactionProcessor processor = new TransactionProcessor();
        assertFalse(processor.reject());
    }
}
