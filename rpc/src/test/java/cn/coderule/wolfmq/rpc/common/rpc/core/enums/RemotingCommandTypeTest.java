package cn.coderule.wolfmq.rpc.common.rpc.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemotingCommandTypeTest {

    @Test
    void testEnumValues() {
        assertEquals(2, RemotingCommandType.values().length);
    }

    @Test
    void testRequestCommand() {
        assertNotNull(RemotingCommandType.REQUEST_COMMAND);
        assertEquals("REQUEST_COMMAND", RemotingCommandType.REQUEST_COMMAND.name());
    }

    @Test
    void testResponseCommand() {
        assertNotNull(RemotingCommandType.RESPONSE_COMMAND);
        assertEquals("RESPONSE_COMMAND", RemotingCommandType.RESPONSE_COMMAND.name());
    }

    @Test
    void testValueOf() {
        assertEquals(RemotingCommandType.REQUEST_COMMAND, RemotingCommandType.valueOf("REQUEST_COMMAND"));
        assertEquals(RemotingCommandType.RESPONSE_COMMAND, RemotingCommandType.valueOf("RESPONSE_COMMAND"));
    }
}