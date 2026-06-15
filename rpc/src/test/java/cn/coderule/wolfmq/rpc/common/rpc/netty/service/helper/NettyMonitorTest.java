package cn.coderule.wolfmq.rpc.common.rpc.netty.service.helper;

import cn.coderule.wolfmq.domain.config.network.RpcServerConfig;
import cn.coderule.wolfmq.rpc.common.rpc.netty.handler.RequestCodeCounter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NettyMonitorTest {

    @Test
    void start_WithNullCounter_ShouldNotThrow() {
        RpcServerConfig config = mock(RpcServerConfig.class);
        NettyMonitor monitor = new NettyMonitor(config, null);

        assertDoesNotThrow(() -> monitor.start());
        monitor.shutdown();
    }

    @Test
    void start_WithCounter_ShouldNotThrow() {
        RpcServerConfig config = mock(RpcServerConfig.class);
        RequestCodeCounter counter = new RequestCodeCounter();
        NettyMonitor monitor = new NettyMonitor(config, counter);

        assertDoesNotThrow(() -> monitor.start());
        monitor.shutdown();
    }

    @Test
    void shutdown_ShouldNotThrow() {
        RpcServerConfig config = mock(RpcServerConfig.class);
        NettyMonitor monitor = new NettyMonitor(config, null);

        assertDoesNotThrow(() -> monitor.shutdown());
    }
}