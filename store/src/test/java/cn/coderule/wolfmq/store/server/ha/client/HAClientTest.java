package cn.coderule.wolfmq.store.server.ha.client;

import cn.coderule.wolfmq.store.server.ha.core.ConnectionState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HAClientTest {

    @Test
    void testHAClientInterface() throws Exception {
        HAClient client = mock(HAClient.class);

        when(client.getConnectionState()).thenReturn(ConnectionState.READY);
        when(client.getMasterAddress()).thenReturn("127.0.0.1:8080");
        when(client.getMasterHaAddress()).thenReturn("127.0.0.1:8081");
        when(client.connectMaster()).thenReturn(true);

        assertEquals(ConnectionState.READY, client.getConnectionState());
        assertEquals("127.0.0.1:8080", client.getMasterAddress());
        assertEquals("127.0.0.1:8081", client.getMasterHaAddress());
        assertTrue(client.connectMaster());

        client.changeConnectionState(ConnectionState.SHUTDOWN);
        client.setMasterAddress("127.0.0.1:9090");
        client.setMasterHaAddress("127.0.0.1:9091");
        client.wakeup();
        client.closeMaster();

        verify(client).changeConnectionState(ConnectionState.SHUTDOWN);
        verify(client).setMasterAddress("127.0.0.1:9090");
        verify(client).setMasterHaAddress("127.0.0.1:9091");
        verify(client).wakeup();
        verify(client).closeMaster();
    }
}