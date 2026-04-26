package cn.coderule.wolfmq.store.server.ha.server.socket;

import cn.coderule.wolfmq.store.server.ha.core.HAContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DefaultAcceptServiceTest {

    @Test
    void testGetServiceName() {
        HAContext haContext = mock(HAContext.class);
        DefaultAcceptService service = new DefaultAcceptService(haContext);
        assertEquals("DefaultAcceptService", service.getServiceName());
    }
}