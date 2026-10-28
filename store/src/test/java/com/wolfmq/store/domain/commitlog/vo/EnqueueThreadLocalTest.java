package com.wolfmq.store.domain.commitlog.vo;

import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.domain.message.codec.MessageEncoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnqueueThreadLocalTest {

    @Test
    void testConstructor() {
        MessageConfig config = new MessageConfig();

        EnqueueThreadLocal threadLocal = new EnqueueThreadLocal(config);

        assertNotNull(threadLocal);
        assertNotNull(threadLocal.getEncoder());
    }

    @Test
    void testGetEncoder() {
        MessageConfig config = new MessageConfig();

        EnqueueThreadLocal threadLocal = new EnqueueThreadLocal(config);
        MessageEncoder encoder1 = threadLocal.getEncoder();
        MessageEncoder encoder2 = threadLocal.getEncoder();

        assertNotNull(encoder1);
        assertSame(encoder1, encoder2);
    }

}
