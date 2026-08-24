package cn.coderule.wolfmq.broker.domain.consumer.renew;

import cn.coderule.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import cn.coderule.wolfmq.broker.infra.store.SubscriptionStore;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RenewServiceTest {

    @Test
    void constructor_ShouldCreateService() {
        BrokerConfig brokerConfig = ConfigMock.createBrokerConfig();
        ReceiptHandler receiptHandler = mock(ReceiptHandler.class);
        RenewListener renewListener = mock(RenewListener.class);
        ConsumerManager consumerManager = mock(ConsumerManager.class);
        SubscriptionStore subscriptionStore = mock(SubscriptionStore.class);

        RenewService service = new RenewService(
            brokerConfig, receiptHandler, renewListener, consumerManager, subscriptionStore
        );
        assertNotNull(service);
    }

    @Test
    void shutdown_ShouldNotThrow() {
        BrokerConfig brokerConfig = ConfigMock.createBrokerConfig();
        ReceiptHandler receiptHandler = mock(ReceiptHandler.class);
        RenewListener renewListener = mock(RenewListener.class);
        ConsumerManager consumerManager = mock(ConsumerManager.class);
        SubscriptionStore subscriptionStore = mock(SubscriptionStore.class);

        RenewService service = new RenewService(
            brokerConfig, receiptHandler, renewListener, consumerManager, subscriptionStore
        );
        assertDoesNotThrow(() -> service.shutdown());
    }
}
