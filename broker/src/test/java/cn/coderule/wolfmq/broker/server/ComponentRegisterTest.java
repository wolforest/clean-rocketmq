package cn.coderule.wolfmq.broker.server;

import cn.coderule.wolfmq.broker.server.bootstrap.BrokerContext;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentRegisterTest {

    @BeforeEach
    void setUp() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        BrokerContext.APPLICATION.getObjectMap().clear();
        BrokerContext.API.getObjectMap().clear();
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        BrokerContext.APPLICATION.getObjectMap().clear();
        BrokerContext.API.getObjectMap().clear();
    }

    @Test
    void constructor_ShouldCreateComponentRegister() {
        BrokerConfig brokerConfig = ConfigMock.createBrokerConfig();
        BrokerContext.register(brokerConfig);

        ComponentRegister register = new ComponentRegister();
        assertNotNull(register);
    }
}
