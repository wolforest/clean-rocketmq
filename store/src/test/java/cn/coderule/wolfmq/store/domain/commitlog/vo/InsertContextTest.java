package cn.coderule.wolfmq.store.domain.commitlog.vo;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFile;
import cn.coderule.wolfmq.domain.domain.message.MessageEncoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InsertContextTest {

    @Test
    void testDefaultConstructor() {
        InsertContext context = new InsertContext();

        assertNotNull(context);
        assertEquals(0L, context.getNow());
        assertNull(context.getMessageBO());
        assertNull(context.getResult());
        assertEquals(0L, context.getElapsedTimeInLock());
        assertNull(context.getMappedFile());
        assertNull(context.getEncoder());
    }

    @Test
    void testBuilder() {
        MessageBO messageBO = mock(MessageBO.class);
        MappedFile mappedFile = mock(MappedFile.class);
        MessageEncoder encoder = mock(MessageEncoder.class);
        long now = System.currentTimeMillis();

        InsertContext context = InsertContext.builder()
            .now(now)
            .messageBO(messageBO)
            .mappedFile(mappedFile)
            .encoder(encoder)
            .build();

        assertEquals(now, context.getNow());
        assertSame(messageBO, context.getMessageBO());
        assertSame(mappedFile, context.getMappedFile());
        assertSame(encoder, context.getEncoder());
        assertNull(context.getResult());
        assertEquals(0L, context.getElapsedTimeInLock());
    }

    @Test
    void testBuilderWithResult() {
        EnqueueResult result = mock(EnqueueResult.class);

        InsertContext context = InsertContext.builder()
            .result(result)
            .build();

        assertSame(result, context.getResult());
    }

    @Test
    void testSettersAndGetters() {
        InsertContext context = new InsertContext();
        MessageBO messageBO = mock(MessageBO.class);
        MappedFile mappedFile = mock(MappedFile.class);
        EnqueueResult result = mock(EnqueueResult.class);

        context.setNow(12345L);
        context.setMessageBO(messageBO);
        context.setElapsedTimeInLock(100L);
        context.setMappedFile(mappedFile);
        context.setResult(result);

        assertEquals(12345L, context.getNow());
        assertSame(messageBO, context.getMessageBO());
        assertEquals(100L, context.getElapsedTimeInLock());
        assertSame(mappedFile, context.getMappedFile());
        assertSame(result, context.getResult());
    }
}
