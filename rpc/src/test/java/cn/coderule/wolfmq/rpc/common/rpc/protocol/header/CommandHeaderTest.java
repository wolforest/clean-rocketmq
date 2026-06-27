package cn.coderule.wolfmq.rpc.common.rpc.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandHeaderTest {

    static class TestCommandHeader implements CommandHeader {
        @Override
        public void checkFields() throws RemotingCommandException {
        }
    }

    @Test
    void checkFields_ShouldNotThrow() throws RemotingCommandException {
        TestCommandHeader header = new TestCommandHeader();
        assertDoesNotThrow(() -> header.checkFields());
    }

    @Test
    void interfaceCanBeImplemented() {
        CommandHeader header = new TestCommandHeader();
        assertNotNull(header);
    }
}