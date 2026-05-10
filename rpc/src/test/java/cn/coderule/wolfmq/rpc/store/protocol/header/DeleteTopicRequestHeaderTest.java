package cn.coderule.wolfmq.rpc.store.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeleteTopicRequestHeaderTest {

    @Test
    void testConstructor() {
        DeleteTopicRequestHeader header = new DeleteTopicRequestHeader();
        assertNotNull(header);
    }

    @Test
    void testTopicGetterAndSetter() {
        DeleteTopicRequestHeader header = new DeleteTopicRequestHeader();
        header.setTopic("testTopic");

        assertEquals("testTopic", header.getTopic());
    }

    @Test
    void testCheckFields_doesNotThrow() {
        DeleteTopicRequestHeader header = new DeleteTopicRequestHeader();
        header.setTopic("testTopic");

        assertDoesNotThrow(() -> header.checkFields());
    }
}