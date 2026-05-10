package cn.coderule.wolfmq.broker.infra.store;

import cn.coderule.wolfmq.broker.infra.embed.EmbedSubscriptionStore;
import cn.coderule.wolfmq.broker.infra.remote.RemoteSubscriptionStore;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionStoreTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private EmbedSubscriptionStore embedSubscriptionStore;

    @Mock
    private RemoteSubscriptionStore remoteSubscriptionStore;

    private SubscriptionStore subscriptionStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscriptionStore = new SubscriptionStore(brokerConfig, embedSubscriptionStore, remoteSubscriptionStore);
    }

    @Test
    void testConstructor() {
        assertNotNull(subscriptionStore);
    }

    @Test
    void testExistsGroup_embedStoreTrue() {
        when(embedSubscriptionStore.existsGroup("testTopic", "testGroup")).thenReturn(true);

        boolean result = subscriptionStore.existsGroup("testTopic", "testGroup");

        assertTrue(result);
    }

    @Test
    void testExistsGroup_remoteDisabled() {
        when(embedSubscriptionStore.existsGroup("testTopic", "testGroup")).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        boolean result = subscriptionStore.existsGroup("testTopic", "testGroup");

        assertFalse(result);
    }

    @Test
    void testExistsGroup_remoteStoreTrue() {
        when(embedSubscriptionStore.existsGroup("testTopic", "testGroup")).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteSubscriptionStore.existsGroup("testTopic", "testGroup")).thenReturn(true);

        boolean result = subscriptionStore.existsGroup("testTopic", "testGroup");

        assertTrue(result);
    }
}