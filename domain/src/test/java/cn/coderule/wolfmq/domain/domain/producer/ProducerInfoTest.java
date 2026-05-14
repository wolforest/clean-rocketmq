package cn.coderule.wolfmq.domain.domain.producer;

import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProducerInfoTest {

    @Test
    void testSetterGetter() {
        ProducerInfo info = new ProducerInfo();
        info.setGroupName("producerGroup");
        
        ClientChannelInfo channelInfo = mock(ClientChannelInfo.class);
        info.setChannelInfo(channelInfo);

        assertEquals("producerGroup", info.getGroupName());
        assertEquals(channelInfo, info.getChannelInfo());
    }

    @Test
    void testConstructorWithSetters() {
        ClientChannelInfo channelInfo = mock(ClientChannelInfo.class);
        ProducerInfo info = new ProducerInfo();
        info.setGroupName("group");
        info.setChannelInfo(channelInfo);

        assertEquals("group", info.getGroupName());
        assertEquals(channelInfo, info.getChannelInfo());
    }
}