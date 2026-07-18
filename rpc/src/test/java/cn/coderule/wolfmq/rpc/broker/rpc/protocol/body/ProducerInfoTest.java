package cn.coderule.wolfmq.rpc.broker.rpc.protocol.body;

import cn.coderule.wolfmq.domain.core.enums.code.LanguageCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProducerInfoTest {

    @Test
    void constructorAndGetters() {
        ProducerInfo info = new ProducerInfo("client-1", "127.0.0.1", LanguageCode.JAVA, 1, 100L);

        assertEquals("client-1", info.getClientId());
        assertEquals("127.0.0.1", info.getRemoteIP());
        assertEquals(LanguageCode.JAVA, info.getLanguage());
        assertEquals(1, info.getVersion());
        assertEquals(100L, info.getLastUpdateTimestamp());
    }

    @Test
    void setters() {
        ProducerInfo info = new ProducerInfo("c1", "ip", LanguageCode.JAVA, 0, 0L);
        info.setClientId("client-2");
        info.setRemoteIP("192.168.1.1");
        info.setLanguage(LanguageCode.CPP);
        info.setVersion(2);
        info.setLastUpdateTimestamp(200L);

        assertEquals("client-2", info.getClientId());
        assertEquals("192.168.1.1", info.getRemoteIP());
        assertEquals(LanguageCode.CPP, info.getLanguage());
        assertEquals(2, info.getVersion());
        assertEquals(200L, info.getLastUpdateTimestamp());
    }
}