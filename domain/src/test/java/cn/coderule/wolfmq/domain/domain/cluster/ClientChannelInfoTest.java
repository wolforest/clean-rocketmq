package cn.coderule.wolfmq.domain.domain.cluster;

import cn.coderule.wolfmq.domain.core.enums.code.LanguageCode;
import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientChannelInfoTest {

    @Test
    void testBuilder_createsClientChannelInfo() {
        Channel channel = mock(Channel.class);
        ClientChannelInfo info = ClientChannelInfo.builder()
            .channel(channel)
            .clientId("client-123")
            .language(LanguageCode.JAVA)
            .version(1)
            .build();

        assertNotNull(info);
        assertEquals(channel, info.getChannel());
        assertEquals("client-123", info.getClientId());
        assertEquals(LanguageCode.JAVA, info.getLanguage());
        assertEquals(1, info.getVersion());
    }

    @Test
    void testConstructor_withChannelOnly() {
        Channel channel = mock(Channel.class);
        ClientChannelInfo info = new ClientChannelInfo(channel);

        assertEquals(channel, info.getChannel());
        assertNull(info.getClientId());
        assertNull(info.getLanguage());
        assertEquals(0, info.getVersion());
    }

    @Test
    void testConstructor_withAllParameters() {
        Channel channel = mock(Channel.class);
        ClientChannelInfo info = new ClientChannelInfo(channel, "client-456", LanguageCode.GO, 2);

        assertEquals(channel, info.getChannel());
        assertEquals("client-456", info.getClientId());
        assertEquals(LanguageCode.GO, info.getLanguage());
        assertEquals(2, info.getVersion());
    }

    @Test
    void testLastUpdateTime_initializesToCurrentTime() {
        Channel channel = mock(Channel.class);
        ClientChannelInfo info = new ClientChannelInfo(channel);

        assertTrue(info.getLastUpdateTime() > 0);
    }
}