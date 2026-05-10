package cn.coderule.wolfmq.rpc.store.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetSubscriptionGroupConfigRequestHeaderTest {

    @Test
    void testConstructor() {
        GetSubscriptionGroupConfigRequestHeader header = new GetSubscriptionGroupConfigRequestHeader();
        assertNotNull(header);
    }

    @Test
    void testGroupNameGetterAndSetter() {
        GetSubscriptionGroupConfigRequestHeader header = new GetSubscriptionGroupConfigRequestHeader();
        header.setGroup("testGroup");

        assertEquals("testGroup", header.getGroup());
    }

    @Test
    void testCheckFields_doesNotThrow() {
        GetSubscriptionGroupConfigRequestHeader header = new GetSubscriptionGroupConfigRequestHeader();

        assertDoesNotThrow(() -> header.checkFields());
    }
}