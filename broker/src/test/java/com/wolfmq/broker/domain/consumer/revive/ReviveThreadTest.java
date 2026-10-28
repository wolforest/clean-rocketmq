package com.wolfmq.broker.domain.consumer.revive;

import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.rpc.store.facade.ConsumeOffsetFacade;
import com.wolfmq.rpc.store.facade.MQFacade;
import com.wolfmq.rpc.store.facade.SubscriptionFacade;
import com.wolfmq.rpc.store.facade.TopicFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviveThreadTest {

    @Test
    void getServiceName_ShouldReturnClassName() {
        BrokerConfig brokerConfig = ConfigMock.createBrokerConfig();
        ReviveContext context = ReviveContext.builder()
            .brokerConfig(brokerConfig)
            .reviveTopic("revive-topic")
            .mqFacade(mock(MQFacade.class))
            .topicFacade(mock(TopicFacade.class))
            .subscriptionFacade(mock(SubscriptionFacade.class))
            .consumeOffsetFacade(mock(ConsumeOffsetFacade.class))
            .build();
        RetryService retryService = mock(RetryService.class);

        ReviveThread thread = new ReviveThread(context, 0, retryService);
        assertEquals("ReviveThread", thread.getServiceName());
    }
}
