package cn.coderule.wolfmq.store.server.ha.server.socket;

import cn.coderule.wolfmq.store.server.ha.core.HAConnection;
import cn.coderule.wolfmq.store.server.ha.core.HAContext;
import org.junit.jupiter.api.Test;

import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AbstractAcceptServiceTest {

    @Test
    void testGetServiceName() {
        HAContext haContext = mock(HAContext.class);
        TestAcceptService service = new TestAcceptService(haContext);
        assertEquals("TestAcceptService", service.getServiceName());
    }

    @Test
    void testShutdownDoesNotThrow() {
        HAContext haContext = mock(HAContext.class);
        TestAcceptService service = new TestAcceptService(haContext);
        assertDoesNotThrow(() -> service.shutdown(false));
    }

    private static class TestAcceptService extends AbstractAcceptService {
        protected TestAcceptService(HAContext haContext) {
            super(haContext);
        }

        @Override
        protected HAConnection createConnection(SocketChannel sc) {
            return null;
        }

        @Override
        public String getServiceName() {
            return "TestAcceptService";
        }
    }
}