package cn.coderule.wolfmq.broker.server.grpc.service.channel;

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
            mock(cn.coderule.wolfmq.broker.api.RouteController.class),
            mock(cn.coderule.wolfmq.broker.api.ProducerController.class),
            mock(cn.coderule.wolfmq.broker.api.ConsumerController.class),
            mock(cn.coderule.wolfmq.broker.api.TransactionController.class)
        ));
    }
}
