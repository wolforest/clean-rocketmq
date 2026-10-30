package com.wolfmq.broker.server;

import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.mock.ConfigMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentRegisterTest {

    @Test
    void constructor_ShouldCreateComponentRegister() {
        ApplicationContext context = new ApplicationContext();
        BrokerConfig brokerConfig = ConfigMock.createBrokerConfig();
        context.register(brokerConfig);

        ComponentRegister register = new ComponentRegister(context);
        assertNotNull(register);
    }
}
