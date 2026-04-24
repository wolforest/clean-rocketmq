package cn.coderule.wolfmq.store.server.ha.server.processor;

import cn.coderule.wolfmq.store.server.ha.core.HAConnection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SlaveOffsetReceiverTest {

    @Test
    void testGetServiceName() {
        HAConnection connection = mock(HAConnection.class);
        SlaveOffsetReceiver receiver = new SlaveOffsetReceiver(connection);
        assertEquals("SlaveOffsetReceiver", receiver.getServiceName());
    }
}