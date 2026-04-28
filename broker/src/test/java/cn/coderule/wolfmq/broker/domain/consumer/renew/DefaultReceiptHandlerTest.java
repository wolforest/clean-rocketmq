package cn.coderule.wolfmq.broker.domain.consumer.renew;

import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultReceiptHandlerTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private RenewListener renewListener;

    @Mock
    private Channel mockChannel;

    @Mock
    private ChannelId mockChannelId;

    private DefaultReceiptHandler receiptHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setInvisibleTimeOfClear(30000);
        when(brokerConfig.getMessageConfig()).thenReturn(messageConfig);
        when(mockChannel.id()).thenReturn(mockChannelId);
        when(mockChannelId.asLongText()).thenReturn("mockChannelId");
        receiptHandler = new DefaultReceiptHandler(brokerConfig, renewListener);
    }

    @Test
    void testGetEntrySetEmpty() {
        Set<java.util.Map.Entry<ReceiptHandleGroupKey, cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandleGroup>> entries = receiptHandler.getEntrySet();
        assertTrue(entries.isEmpty());
    }

    @Test
    void testRemoveGroupNullKey() {
        assertDoesNotThrow(() -> receiptHandler.removeGroup(null));
    }

    @Test
    void testStart() {
        assertDoesNotThrow(() -> receiptHandler.start());
    }

    @Test
    void testShutdownEmpty() {
        assertDoesNotThrow(() -> receiptHandler.shutdown());
    }
}
