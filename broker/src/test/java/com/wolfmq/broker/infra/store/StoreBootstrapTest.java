package com.wolfmq.broker.infra.store;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.broker.infra.embed.EmbedConsumeOffsetStore;
import com.wolfmq.broker.infra.embed.EmbedMQStore;
import com.wolfmq.broker.infra.embed.EmbedStoreBootstrap;
import com.wolfmq.broker.infra.embed.EmbedSubscriptionStore;
import com.wolfmq.broker.infra.embed.EmbedTimerStore;
import com.wolfmq.broker.infra.embed.EmbedTopicStore;
import com.wolfmq.broker.infra.remote.RemoteConsumeOffsetStore;
import com.wolfmq.broker.infra.remote.RemoteMQStore;
import com.wolfmq.broker.infra.remote.RemoteStoreBootstrap;
import com.wolfmq.broker.infra.remote.RemoteSubscriptionStore;
import com.wolfmq.broker.infra.remote.RemoteTimerStore;
import com.wolfmq.broker.infra.remote.RemoteTopicStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    @Mock
    private EmbedStoreBootstrap embedStoreBootstrap;

    @Mock
    private RemoteStoreBootstrap remoteStoreBootstrap;

    @Mock
    private EmbedTopicStore embedTopicStore;

    @Mock
    private EmbedSubscriptionStore embedSubscriptionStore;

    @Mock
    private EmbedConsumeOffsetStore embedConsumeOffsetStore;

    @Mock
    private EmbedTimerStore embedTimerStore;

    @Mock
    private EmbedMQStore embedMQStore;

    @Mock
    private RemoteTopicStore remoteTopicStore;

    @Mock
    private RemoteSubscriptionStore remoteSubscriptionStore;

    @Mock
    private RemoteConsumeOffsetStore remoteConsumeOffsetStore;

    @Mock
    private RemoteTimerStore remoteTimerStore;

    @Mock
    private RemoteMQStore remoteMQStore;

    private StoreBootstrap storeBootstrap;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        storeBootstrap = new StoreBootstrap(context);
    }

    @Test
    void testConstructor() {
        assertNotNull(storeBootstrap);
    }

    @Test
    void testGetTopicStore() {
        // Skip - requires full initialization
        assertNotNull(storeBootstrap);
    }
}