package cn.coderule.wolfmq.broker.domain.producer;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import io.netty.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProducerManagerTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private Channel mockChannel;

    private ProducerManager producerManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(brokerConfig.getChannelExpireTime()).thenReturn(120000L);
        when(brokerConfig.getMaxChannelFetchTimes()).thenReturn(3);
        producerManager = new ProducerManager(brokerConfig);
    }

    @Test
    void testRegister() {
        String groupName = "testGroup";
        ClientChannelInfo channelInfo = createChannelInfo("client1");

        producerManager.register(groupName, channelInfo);

        assertTrue(producerManager.isGroupExist(groupName));
        assertEquals(1, producerManager.getGroupCount());
    }

    @Test
    void testRegisterSameChannelTwice() {
        String groupName = "testGroup";
        ClientChannelInfo channelInfo = createChannelInfo("client1");

        producerManager.register(groupName, channelInfo);
        producerManager.register(groupName, channelInfo);

        assertEquals(1, producerManager.getGroupCount());
    }

    @Test
    void testUnregister() {
        String groupName = "testGroup";
        ClientChannelInfo channelInfo = createChannelInfo("client1");

        producerManager.register(groupName, channelInfo);
        producerManager.unregister(groupName, channelInfo);

        assertFalse(producerManager.isGroupExist(groupName));
        assertEquals(0, producerManager.getGroupCount());
    }

    @Test
    void testUnregisterNonExistentGroup() {
        String groupName = "nonExistentGroup";
        ClientChannelInfo channelInfo = createChannelInfo("client1");

        assertDoesNotThrow(() -> producerManager.unregister(groupName, channelInfo));
    }

    @Test
    void testGetGroupCount() {
        assertEquals(0, producerManager.getGroupCount());

        producerManager.register("group1", createChannelInfo("client1"));
        assertEquals(1, producerManager.getGroupCount());

        producerManager.register("group2", createChannelInfo("client2"));
        assertEquals(2, producerManager.getGroupCount());
    }

    @Test
    void testIsGroupExist() {
        String groupName = "testGroup";
        assertFalse(producerManager.isGroupExist(groupName));

        producerManager.register(groupName, createChannelInfo("client1"));
        assertTrue(producerManager.isGroupExist(groupName));
    }

    private ClientChannelInfo createChannelInfo(String clientId) {
        when(mockChannel.isActive()).thenReturn(true);
        when(mockChannel.isWritable()).thenReturn(true);

        return ClientChannelInfo.builder()
            .clientId(clientId)
            .channel(mockChannel)
            .lastUpdateTime(System.currentTimeMillis())
            .build();
    }
}
