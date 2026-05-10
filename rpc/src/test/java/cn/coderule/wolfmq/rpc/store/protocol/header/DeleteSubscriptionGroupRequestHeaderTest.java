package cn.coderule.wolfmq.rpc.store.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeleteSubscriptionGroupRequestHeaderTest {

    @Test
    void testConstructor() {
        DeleteSubscriptionGroupRequestHeader header = new DeleteSubscriptionGroupRequestHeader();
        assertNotNull(header);
    }

    @Test
    void testGroupNameGetterAndSetter() {
        DeleteSubscriptionGroupRequestHeader header = new DeleteSubscriptionGroupRequestHeader();
        header.setGroupName("testGroup");

        assertEquals("testGroup", header.getGroupName());
    }

    @Test
    void testCheckFields_doesNotThrow() {
        DeleteSubscriptionGroupRequestHeader header = new DeleteSubscriptionGroupRequestHeader();

        assertDoesNotThrow(() -> header.checkFields());
    }
}