package cn.coderule.wolfmq.rpc.store.protocol.header;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetAllTopicConfigResponseHeaderTest {

    @Test
    void testConstructor() {
        GetAllTopicConfigResponseHeader header = new GetAllTopicConfigResponseHeader();
        assertNotNull(header);
    }

    @Test
    void testCheckFields() throws Exception {
        GetAllTopicConfigResponseHeader header = new GetAllTopicConfigResponseHeader();
        header.checkFields();
    }
}