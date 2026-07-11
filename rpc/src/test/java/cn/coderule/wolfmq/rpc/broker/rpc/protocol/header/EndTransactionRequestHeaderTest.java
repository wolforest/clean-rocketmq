package cn.coderule.wolfmq.rpc.broker.rpc.protocol.header;

import cn.coderule.wolfmq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EndTransactionRequestHeaderTest {

    @Test
    void gettersAndSetters() {
        EndTransactionRequestHeader header = new EndTransactionRequestHeader();
        header.setTopic("test-topic");
        header.setTransactionId("txn-1");
        header.setCommitLogOffset(100L);
        header.setCommitOrRollback(MessageSysFlag.COMMIT_MESSAGE);
        header.setProducerGroup("pg-1");
        header.setMsgId("msg-1");

        assertEquals("test-topic", header.getTopic());
        assertEquals("txn-1", header.getTransactionId());
        assertEquals(100L, header.getCommitLogOffset());
        assertEquals(MessageSysFlag.COMMIT_MESSAGE, header.getCommitOrRollback());
        assertEquals("pg-1", header.getProducerGroup());
        assertEquals("msg-1", header.getMsgId());
    }

    @Test
    void checkFields_WithCommitMessage_ShouldNotThrow() throws RemotingCommandException {
        EndTransactionRequestHeader header = new EndTransactionRequestHeader();
        header.setCommitOrRollback(MessageSysFlag.COMMIT_MESSAGE);
        assertDoesNotThrow(() -> header.checkFields());
    }

    @Test
    void checkFields_WithRollbackMessage_ShouldNotThrow() throws RemotingCommandException {
        EndTransactionRequestHeader header = new EndTransactionRequestHeader();
        header.setCommitOrRollback(MessageSysFlag.ROLLBACK_MESSAGE);
        assertDoesNotThrow(() -> header.checkFields());
    }

    @Test
    void checkFields_WithNormalMessage_ShouldNotThrow() throws RemotingCommandException {
        EndTransactionRequestHeader header = new EndTransactionRequestHeader();
        header.setCommitOrRollback(MessageSysFlag.NORMAL_MESSAGE);
        assertDoesNotThrow(() -> header.checkFields());
    }
}