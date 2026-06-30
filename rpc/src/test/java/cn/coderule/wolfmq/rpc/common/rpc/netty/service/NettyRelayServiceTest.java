package cn.coderule.wolfmq.rpc.common.rpc.netty.service;

import cn.coderule.wolfmq.rpc.common.core.relay.request.ConsumeRequest;
import cn.coderule.wolfmq.rpc.common.core.relay.request.ConsumerRequest;
import cn.coderule.wolfmq.rpc.common.core.relay.request.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NettyRelayServiceTest {

    private NettyRelayService service;

    @BeforeEach
    void setUp() {
        service = new NettyRelayService();
    }

    @Test
    void getConsumerInfo_ShouldReturnNull() {
        assertNull(service.getConsumerInfo(null));
    }

    @Test
    void consumeMessage_ShouldReturnNull() {
        assertNull(service.consumeMessage(null));
    }

    @Test
    void checkTransaction_ShouldReturnNull() {
        assertNull(service.checkTransaction(null));
    }
}