package cn.coderule.wolfmq.store.server.ha.core.hook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HAReadHookTest {

    @Test
    void testInterfaceHasAfterReadMethod() {
        HAReadHook hook = readSize -> {};
        assertNotNull(hook);
    }

    @Test
    void testLambdaImplementation() {
        int[] capturedSize = {0};
        HAReadHook hook = readSize -> capturedSize[0] = readSize;
        hook.afterRead(1024);
        assertEquals(1024, capturedSize[0]);
    }

    @Test
    void testMultipleImplementations() {
        HAReadHook noOp = readSize -> {};
        HAReadHook tracking = readSize -> {};

        assertDoesNotThrow(() -> noOp.afterRead(100));
        assertDoesNotThrow(() -> tracking.afterRead(200));
    }
}