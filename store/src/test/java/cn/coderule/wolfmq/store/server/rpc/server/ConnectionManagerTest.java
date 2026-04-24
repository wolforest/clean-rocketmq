package cn.coderule.wolfmq.store.server.rpc.server;

import cn.coderule.wolfmq.rpc.common.rpc.RpcListener;
import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionManagerTest {

    @Test
    void testImplementsRpcListener() {
        ConnectionManager manager = new ConnectionManager();
        assertInstanceOf(RpcListener.class, manager);
    }

    @Test
    void testOnConnectDoesNotThrow() {
        ConnectionManager manager = new ConnectionManager();
        Channel channel = null;
        assertDoesNotThrow(() -> manager.onConnect("127.0.0.1:9000", channel));
    }

    @Test
    void testOnCloseDoesNotThrow() {
        ConnectionManager manager = new ConnectionManager();
        Channel channel = null;
        assertDoesNotThrow(() -> manager.onClose("127.0.0.1:9000", channel));
    }

    @Test
    void testOnExceptionDoesNotThrow() {
        ConnectionManager manager = new ConnectionManager();
        Channel channel = null;
        assertDoesNotThrow(() -> manager.onException("127.0.0.1:9000", channel));
    }

    @Test
    void testOnIdleDoesNotThrow() {
        ConnectionManager manager = new ConnectionManager();
        Channel channel = null;
        assertDoesNotThrow(() -> manager.onIdle("127.0.0.1:9000", channel));
    }

    @Test
    void testOnActiveDoesNotThrow() {
        ConnectionManager manager = new ConnectionManager();
        Channel channel = null;
        assertDoesNotThrow(() -> manager.onActive("127.0.0.1:9000", channel));
    }
}