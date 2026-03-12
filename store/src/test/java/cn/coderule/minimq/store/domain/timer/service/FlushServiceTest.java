package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.test.ConfigMock;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlushServiceTest {

    @Test
    void getServiceName_ShouldReturnClassName(@TempDir Path tempDir) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        TimerService timerService = mock(TimerService.class);
        FlushService flushService = new FlushService(storeConfig, timerService);

        assertEquals("FlushService", flushService.getServiceName());
        assertDoesNotThrow(flushService::run);
    }
}

