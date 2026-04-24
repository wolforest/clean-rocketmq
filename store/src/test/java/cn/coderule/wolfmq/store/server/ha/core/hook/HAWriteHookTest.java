package cn.coderule.wolfmq.store.server.ha.core.hook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HAWriteHookTest {

    @Test
    void testInterfaceHasAfterWriteMethod() {
        HAWriteHook hook = writeSize -> {};
        assertNotNull(hook);
    }

    @Test
    void testLambdaImplementation() {
        int[] capturedSize = {0};
        HAWriteHook hook = writeSize -> capturedSize[0] = writeSize;
        hook.afterWrite(2048);
        assertEquals(2048, capturedSize[0]);
    }

    @Test
    void testMultipleImplementations() {
        HAWriteHook noOp = writeSize -> {};
        HAWriteHook tracking = writeSize -> {};

        assertDoesNotThrow(() -> noOp.afterWrite(100));
        assertDoesNotThrow(() -> tracking.afterWrite(200));
    }
}