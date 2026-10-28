package com.wolfmq.broker.domain.consumer.pop;

import com.wolfmq.rpc.store.facade.MQFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BrokerDequeueServiceTest {

    @Test
    void testConstructor() {
        MQFacade mqStore = mock(MQFacade.class);
        BrokerDequeueService service = new BrokerDequeueService(mqStore);
        assertNotNull(service);
    }
}