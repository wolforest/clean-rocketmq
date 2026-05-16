package cn.coderule.wolfmq.rpc.store.protocol.header;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetTopicConfigRequestHeaderTest {

    @Test
    void testSetters() {
        GetTopicConfigRequestHeader header = new GetTopicConfigRequestHeader();
        header.setTopic("testTopic");
        header.setLo(true);

        assertEquals("testTopic", header.getTopic());
        assertTrue(header.getLo());
    }

    @Test
    void testCheckFields() throws Exception {
        GetTopicConfigRequestHeader header = new GetTopicConfigRequestHeader();
        header.checkFields();
    }
}