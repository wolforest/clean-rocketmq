package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.store.api.meta.SubscriptionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbedSubscriptionStoreTest {

    private EmbedSubscriptionStore store;
    private SubscriptionStore subscriptionStore;
    private EmbedLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        subscriptionStore = mock(SubscriptionStore.class);
        loadBalance = mock(EmbedLoadBalance.class);
        store = new EmbedSubscriptionStore(subscriptionStore, loadBalance);
    }

    @Test
    void constructor_ShouldCreateStore() {
        assertNotNull(store);
    }

    @Test
    void existsGroup_ShouldDelegate() {
        when(subscriptionStore.existsGroup("test-group")).thenReturn(true);
        assertTrue(store.existsGroup("test-topic", "test-group"));
    }

    @Test
    void getGroup_ShouldDelegate() {
        SubscriptionGroup group = new SubscriptionGroup();
        when(subscriptionStore.getGroup("test-group")).thenReturn(group);
        SubscriptionGroup result = store.getGroup("test-topic", "test-group");
        assertNotNull(result);
    }
}
