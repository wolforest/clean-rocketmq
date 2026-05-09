package cn.coderule.wolfmq.rpc.common.core.relay.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumeResultTest {

    @Test
    void testDefaultConstructor() {
        ConsumeResult result = new ConsumeResult();
        
        assertNotNull(result);
    }

    @Test
    void testSerializable() {
        ConsumeResult result = new ConsumeResult();
        
        assertTrue(result instanceof java.io.Serializable);
    }
}
