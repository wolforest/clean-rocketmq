package com.wolfmq.broker.server.grpc.service.consume;

import com.wolfmq.broker.api.ConsumerController;
import com.wolfmq.broker.server.grpc.service.channel.SettingManager;
import com.wolfmq.common.config.server.BrokerConfig;
import com.wolfmq.common.testing.ConfigMock;
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
