package cn.coderule.wolfmq.broker.domain.consumer.renew;

import cn.coderule.wolfmq.domain.core.enums.consume.ConsumerEvent;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReceiptListenerTest {

    @Test
    void testHandleIgnoresNonUnregisterEvent() {
        ReceiptHandler handler = mock(ReceiptHandler.class);
        ReceiptListener listener = new ReceiptListener(handler);

        assertDoesNotThrow(() -> listener.handle(ConsumerEvent.CHANGE, "group"));
        verify(handler, never()).removeGroup(any());
    }

    @Test
    void testHandleIgnoresNullArgs() {
        ReceiptHandler handler = mock(ReceiptHandler.class);
        ReceiptListener listener = new ReceiptListener(handler);

        assertDoesNotThrow(() -> listener.handle(ConsumerEvent.CLIENT_UNREGISTER, "group", (Object[]) null));
        verify(handler, never()).removeGroup(any());
    }

    @Test
    void testHandleIgnoresEmptyArgs() {
        ReceiptHandler handler = mock(ReceiptHandler.class);
        ReceiptListener listener = new ReceiptListener(handler);

        assertDoesNotThrow(() -> listener.handle(ConsumerEvent.CLIENT_UNREGISTER, "group"));
        verify(handler, never()).removeGroup(any());
    }
}