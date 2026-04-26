package cn.coderule.wolfmq.registry.domain.store.model;

import cn.coderule.wolfmq.domain.domain.meta.DataVersion;
import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StoreHealthInfoTest {

    @Test
    void testBuilder() {
        long timestamp = System.currentTimeMillis();
        long timeout = 30000L;
        DataVersion version = new DataVersion();
        Channel channel = mock(Channel.class);
        String haAddr = "127.0.0.1:10912";

        StoreHealthInfo info = StoreHealthInfo.builder()
            .lastUpdateTimestamp(timestamp)
            .heartbeatTimeoutMillis(timeout)
            .dataVersion(version)
            .channel(channel)
            .haServerAddr(haAddr)
            .build();

        assertNotNull(info);
        assertEquals(timestamp, info.getLastUpdateTimestamp());
        assertEquals(timeout, info.getHeartbeatTimeoutMillis());
        assertSame(version, info.getDataVersion());
        assertSame(channel, info.getChannel());
        assertEquals(haAddr, info.getHaServerAddr());
    }

    @Test
    void testDefaultConstructor() {
        StoreHealthInfo info = new StoreHealthInfo();

        assertNotNull(info);
        assertEquals(0L, info.getLastUpdateTimestamp());
        assertEquals(0L, info.getHeartbeatTimeoutMillis());
        assertNull(info.getDataVersion());
        assertNull(info.getChannel());
        assertNull(info.getHaServerAddr());
    }

    @Test
    void testSettersAndGetters() {
        StoreHealthInfo info = new StoreHealthInfo();
        DataVersion version = new DataVersion();
        Channel channel = mock(Channel.class);

        info.setLastUpdateTimestamp(1000L);
        info.setHeartbeatTimeoutMillis(30000L);
        info.setDataVersion(version);
        info.setChannel(channel);
        info.setHaServerAddr("192.168.1.1:10912");

        assertEquals(1000L, info.getLastUpdateTimestamp());
        assertEquals(30000L, info.getHeartbeatTimeoutMillis());
        assertSame(version, info.getDataVersion());
        assertSame(channel, info.getChannel());
        assertEquals("192.168.1.1:10912", info.getHaServerAddr());
    }

    @Test
    void testAllArgsConstructor() {
        DataVersion version = new DataVersion();
        Channel channel = mock(Channel.class);

        StoreHealthInfo info = new StoreHealthInfo(
            2000L, 60000L, version, channel, "10.0.0.1:10912"
        );

        assertEquals(2000L, info.getLastUpdateTimestamp());
        assertEquals(60000L, info.getHeartbeatTimeoutMillis());
        assertSame(version, info.getDataVersion());
        assertSame(channel, info.getChannel());
        assertEquals("10.0.0.1:10912", info.getHaServerAddr());
    }
}
