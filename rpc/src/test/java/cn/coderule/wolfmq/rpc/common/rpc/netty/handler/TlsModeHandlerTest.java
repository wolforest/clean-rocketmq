package cn.coderule.wolfmq.rpc.common.rpc.netty.handler;

import cn.coderule.wolfmq.rpc.common.rpc.core.enums.TlsMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TlsModeHandlerTest {

    @Test
    void handlerName_ShouldBeCorrect() {
        assertEquals("TlsModeHandler", TlsModeHandler.TLS_MODE_HANDLER);
        assertEquals("sslHandler", TlsModeHandler.TLS_HANDLER_NAME);
        assertEquals("fileRegionEncoder", TlsModeHandler.FILE_REGION_ENCODER_NAME);
    }

    @Test
    void constructor_WithDisabledMode() {
        TlsModeHandler handler = new TlsModeHandler(TlsMode.DISABLED, null, null);
        assertNotNull(handler);
    }

    @Test
    void constructor_WithPermissiveMode() {
        TlsModeHandler handler = new TlsModeHandler(TlsMode.PERMISSIVE, null, null);
        assertNotNull(handler);
    }

    @Test
    void constructor_WithEnforcingMode() {
        TlsModeHandler handler = new TlsModeHandler(TlsMode.ENFORCING, null, null);
        assertNotNull(handler);
    }
}