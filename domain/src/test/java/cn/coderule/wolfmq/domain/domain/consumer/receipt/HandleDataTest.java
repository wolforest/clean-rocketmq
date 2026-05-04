package cn.coderule.wolfmq.domain.domain.consumer.receipt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandleDataTest {

    @Test
    void constructor_setsMessageReceipt() {
        MessageReceipt receipt = MessageReceipt.builder()
            .messageId("msg1")
            .queueOffset(100L)
            .build();
        HandleData data = new HandleData(receipt);
        assertEquals(receipt, data.getMessageReceipt());
    }

    @Test
    void lock_unlock() {
        MessageReceipt receipt = MessageReceipt.builder().messageId("msg1").build();
        HandleData data = new HandleData(receipt);
        assertTrue(data.lock(1000));
        data.unlock();
    }

    @Test
    void lock_timeout_fails() throws InterruptedException {
        MessageReceipt receipt = MessageReceipt.builder().messageId("msg1").build();
        HandleData data = new HandleData(receipt);
        assertTrue(data.lock(1000));

        Thread t = new Thread(() -> {
            boolean acquired = data.lock(50);
            assertFalse(acquired);
        });
        t.start();
        t.join();
        data.unlock();
    }

    @Test
    void needRemove_defaultFalse() {
        MessageReceipt receipt = MessageReceipt.builder().messageId("msg1").build();
        HandleData data = new HandleData(receipt);
        assertFalse(data.isNeedRemove());
    }

    @Test
    void needRemove_setTrue() {
        MessageReceipt receipt = MessageReceipt.builder().messageId("msg1").build();
        HandleData data = new HandleData(receipt);
        data.setNeedRemove(true);
        assertTrue(data.isNeedRemove());
    }

    @Test
    void setMessageReceipt() {
        MessageReceipt receipt1 = MessageReceipt.builder().messageId("msg1").build();
        MessageReceipt receipt2 = MessageReceipt.builder().messageId("msg2").build();
        HandleData data = new HandleData(receipt1);
        data.setMessageReceipt(receipt2);
        assertEquals(receipt2, data.getMessageReceipt());
    }
}