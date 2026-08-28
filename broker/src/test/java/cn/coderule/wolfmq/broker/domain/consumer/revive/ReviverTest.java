package cn.coderule.wolfmq.broker.domain.consumer.revive;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.rpc.store.facade.ConsumeOffsetFacade;
import cn.coderule.wolfmq.rpc.store.facade.MQFacade;
import cn.coderule.wolfmq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.wolfmq.rpc.store.facade.TopicFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviverTest {

    @Test
    void constructor_ShouldCreateReviver() {
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

        Reviver reviver = new Reviver(context, 0, retryService);
        assertNotNull(reviver);
    }

    @Test
    void setSkipRevive_ShouldNotThrow() {
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

        Reviver reviver = new Reviver(context, 0, retryService);
        assertDoesNotThrow(() -> reviver.setSkipRevive(true));
    }
}
