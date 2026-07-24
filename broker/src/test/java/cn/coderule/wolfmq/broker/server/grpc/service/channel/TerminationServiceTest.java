package cn.coderule.wolfmq.broker.server.grpc.service.channel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerminationServiceTest {

    @Test
    void constructor_ShouldCreateService() {
        SettingManager settingManager = mock(SettingManager.class);
        ChannelManager channelManager = mock(ChannelManager.class);
        TerminationService service = new TerminationService(settingManager, channelManager);
        assertNotNull(service);
    }

    @Test
    void inject_ShouldNotThrow() {
        SettingManager settingManager = mock(SettingManager.class);
        ChannelManager channelManager = mock(ChannelManager.class);
        TerminationService service = new TerminationService(settingManager, channelManager);

        assertDoesNotThrow(() -> service.inject(
            mock(cn.coderule.wolfmq.broker.api.ProducerController.class),
            mock(cn.coderule.wolfmq.broker.api.ConsumerController.class)
        ));
    }
}
