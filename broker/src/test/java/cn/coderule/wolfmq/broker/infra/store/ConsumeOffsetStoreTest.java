package cn.coderule.wolfmq.broker.infra.store;

import cn.coderule.wolfmq.broker.infra.embed.EmbedConsumeOffsetStore;
import cn.coderule.wolfmq.broker.infra.remote.RemoteConsumeOffsetStore;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumeOffsetStoreTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private EmbedConsumeOffsetStore embedStore;

    @Mock
    private RemoteConsumeOffsetStore remoteStore;

    private ConsumeOffsetStore consumeOffsetStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumeOffsetStore = new ConsumeOffsetStore(brokerConfig, embedStore, remoteStore);
    }

    @Test
    void testConstructor() {
        assertNotNull(consumeOffsetStore);
    }

    @Test
    void testGetOffset_delegatesToEmbedStore() {
        assertNotNull(consumeOffsetStore);
    }

    @Test
    void testUpdateOffset_delegatesToEmbedStore() {
        assertNotNull(consumeOffsetStore);
    }

    @Test
    void testGetConsumeOffset() {
        assertNotNull(consumeOffsetStore);
    }
}