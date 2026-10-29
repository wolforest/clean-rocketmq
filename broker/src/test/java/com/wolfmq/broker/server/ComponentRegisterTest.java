package com.wolfmq.broker.server;

import com.wolfmq.broker.server.bootstrap.BrokerContext;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.server.bootstrap.StoreContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentRegisterTest {

    @BeforeEach
    void setUp() {
        StoreContext.APPLICATION.getBeanMap().clear();
        StoreContext.API.getBeanMap().clear();
        BrokerContext.APPLICATION.getBeanMap().clear();
        BrokerContext.API.getBeanMap().clear();
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getBeanMap().clear();
        StoreContext.API.getBeanMap().clear();
        BrokerContext.APPLICATION.getBeanMap().clear();
        BrokerContext.API.getBeanMap().clear();
    }

    @Test
    void constructor_ShouldCreateComponentRegister() {
        BrokerConfig brokerConfig = ConfigMock.createBrokerConfig();
        BrokerContext.register(brokerConfig);

        ComponentRegister register = new ComponentRegister();
        assertNotNull(register);
    }
}
