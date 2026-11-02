package com.wolfmq.broker.domain.consumer.renew;

import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.broker.infra.store.SubscriptionStore;
import com.wolfmq.common.config.server.BrokerConfig;
import com.wolfmq.common.domain.consumer.receipt.ReceiptHandler;
import com.wolfmq.common.testing.ConfigMock;
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
