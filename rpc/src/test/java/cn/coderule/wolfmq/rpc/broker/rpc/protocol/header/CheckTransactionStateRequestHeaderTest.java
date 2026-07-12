package cn.coderule.wolfmq.rpc.broker.rpc.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckTransactionStateRequestHeaderTest {

    @Test
    void gettersAndSetters() {
        CheckTransactionStateRequestHeader header = new CheckTransactionStateRequestHeader();
        header.setTopic("test-topic");
        header.setTranStateTableOffset(100L);
        header.setCommitLogOffset(200L);
        header.setMsgId("msg-1");
        header.setTransactionId("txn-1");
        header.setOffsetMsgId("offset-1");

        assertEquals("test-topic", header.getTopic());
        assertEquals(100L, header.getTranStateTableOffset());
        assertEquals(200L, header.getCommitLogOffset());
        assertEquals("msg-1", header.getMsgId());
        assertEquals("txn-1", header.getTransactionId());
        assertEquals("offset-1", header.getOffsetMsgId());
    }

    @Test
    void checkFields_ShouldNotThrow() throws RemotingCommandException {
        CheckTransactionStateRequestHeader header = new CheckTransactionStateRequestHeader();
        assertDoesNotThrow(() -> header.checkFields());
    }

    @Test
    void inheritedFields() {
        CheckTransactionStateRequestHeader header = new CheckTransactionStateRequestHeader();
        header.setNamespace("ns");
        header.setBrokerName("bn");
        header.setNamespaced(true);
        header.setOneway(true);

        assertEquals("ns", header.getNamespace());
        assertEquals("bn", header.getBrokerName());
        assertTrue(header.getNamespaced());
        assertTrue(header.getOneway());
    }

    @Test
    void toString_ShouldContainFields() {
        CheckTransactionStateRequestHeader header = new CheckTransactionStateRequestHeader();
        header.setMsgId("msg-1");
        String str = header.toString();
        assertTrue(str.contains("msg-1"));
    }
}