package cn.coderule.wolfmq.broker.server.rpc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrokerServerTest {

    @Test
    void start_ShouldNotThrow() {
        BrokerServer server = new BrokerServer();
        assertDoesNotThrow(() -> server.start());
    }

    @Test
    void shutdown_ShouldNotThrow() {
        BrokerServer server = new BrokerServer();
        assertDoesNotThrow(() -> server.shutdown());
    }

    @Test
    void initialize_ShouldNotThrow() {
        BrokerServer server = new BrokerServer();
        assertDoesNotThrow(() -> server.initialize());
    }
}
