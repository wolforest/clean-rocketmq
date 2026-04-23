package cn.coderule.wolfmq.store.domain.commitlog.vo;

import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.domain.message.MessageEncoder;
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
