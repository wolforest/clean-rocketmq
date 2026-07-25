package cn.coderule.wolfmq.broker.server.grpc.service.channel;

import cn.coderule.wolfmq.broker.api.ConsumerController;
import cn.coderule.wolfmq.broker.api.ProducerController;
import cn.coderule.wolfmq.broker.api.RouteController;
import cn.coderule.wolfmq.broker.api.TransactionController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelemetryServiceTest {

    @Test
    void constructor_ShouldCreateService() {
        SettingManager settingManager = mock(SettingManager.class);
        ChannelManager channelManager = mock(ChannelManager.class);
        TelemetryService service = new TelemetryService(settingManager, channelManager);
        assertNotNull(service);
    }

    @Test
    void inject_ShouldNotThrow() {
        SettingManager settingManager = mock(SettingManager.class);
        ChannelManager channelManager = mock(ChannelManager.class);
        TelemetryService service = new TelemetryService(settingManager, channelManager);

        assertDoesNotThrow(() -> service.inject(
            mock(RouteController.class),
            mock(ProducerController.class),
            mock(ConsumerController.class),
            mock(TransactionController.class)
        ));
    }
}
