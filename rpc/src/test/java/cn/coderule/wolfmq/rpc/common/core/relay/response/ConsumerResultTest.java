package cn.coderule.wolfmq.rpc.common.core.relay.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumerResultTest {

    @Test
    void testDefaultConstructor() {
        ConsumerResult result = new ConsumerResult();
        
        assertNotNull(result);
    }

    @Test
    void testSerializable() {
        ConsumerResult result = new ConsumerResult();
        
        assertTrue(result instanceof java.io.Serializable);
    }
}
