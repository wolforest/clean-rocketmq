package cn.coderule.wolfmq.broker.server.grpc.service.consume;

import cn.coderule.wolfmq.broker.api.ConsumerController;
import cn.coderule.wolfmq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PopServiceTest {

    @Test
    void constructor_ShouldCreateService() {
        BrokerConfig brokerConfig = ConfigMock.createBrokerConfig();
        ConsumerController consumerController = mock(ConsumerController.class);
        SettingManager settingManager = mock(SettingManager.class);

        PopService service = new PopService(brokerConfig, consumerController, settingManager);
        assertNotNull(service);
    }
}
