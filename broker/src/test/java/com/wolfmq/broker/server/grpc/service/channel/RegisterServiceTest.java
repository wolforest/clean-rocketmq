package com.wolfmq.broker.server.grpc.service.channel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterServiceTest {

    @Test
    void constructor_ShouldCreateService() {
        ChannelManager channelManager = mock(ChannelManager.class);
        RegisterService service = new RegisterService(channelManager);
        assertNotNull(service);
    }

    @Test
    void inject_ShouldNotThrow() {
        ChannelManager channelManager = mock(ChannelManager.class);
        RegisterService service = new RegisterService(channelManager);

        assertDoesNotThrow(() -> service.inject(
            mock(com.wolfmq.broker.api.RouteController.class),
            mock(com.wolfmq.broker.api.ProducerController.class),
            mock(com.wolfmq.broker.api.ConsumerController.class),
            mock(com.wolfmq.broker.api.TransactionController.class)
        ));
    }
}
