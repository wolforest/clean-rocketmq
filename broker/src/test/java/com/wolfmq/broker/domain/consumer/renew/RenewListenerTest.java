package com.wolfmq.broker.domain.consumer.renew;

import com.wolfmq.broker.domain.consumer.ack.InvisibleService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RenewListenerTest {

    @Test
    void testConstructor() {
        InvisibleService invisibleService = mock(InvisibleService.class);
        RenewListener listener = new RenewListener(invisibleService);
        assertNotNull(listener);
    }
}