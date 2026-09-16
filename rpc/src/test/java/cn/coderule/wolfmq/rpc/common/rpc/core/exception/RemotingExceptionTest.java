package cn.coderule.wolfmq.rpc.common.rpc.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemotingExceptionTest {

    @Test
    void remotingException_WithMessage() {
        RemotingException ex = new RemotingException("test error");
        assertEquals("test error", ex.getMessage());
    }

    @Test
    void remotingException_WithMessageAndCause() {
        Throwable cause = new RuntimeException("cause");
        RemotingException ex = new RemotingException("test error", cause);
        assertEquals("test error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}

class RemotingCommandExceptionTest {

    @Test
    void constructor_WithMessage() {
        RemotingCommandException ex = new RemotingCommandException("test");
        assertEquals("test", ex.getMessage());
    }
}

class RemotingConnectExceptionTest {

    @Test
    void constructor_WithAddress() {
        RemotingConnectException ex = new RemotingConnectException("127.0.0.1:10911");
        assertTrue(ex.getMessage().contains("127.0.0.1:10911"));
    }
}

class RemotingTimeoutExceptionTest {

    @Test
    void constructor_WithAddressAndTimeout() {
        RemotingTimeoutException ex = new RemotingTimeoutException("127.0.0.1:10911", 3000);
        assertTrue(ex.getMessage().contains("127.0.0.1:10911"));
    }
}

class RemotingSendRequestExceptionTest {

    @Test
    void constructor_WithAddress() {
        RemotingSendRequestException ex = new RemotingSendRequestException("127.0.0.1:10911");
        assertTrue(ex.getMessage().contains("127.0.0.1:10911"));
    }
}

class RemotingTooMuchRequestExceptionTest {

    @Test
    void constructor_WithMessage() {
        RemotingTooMuchRequestException ex = new RemotingTooMuchRequestException("too many");
        assertEquals("too many", ex.getMessage());
    }
}