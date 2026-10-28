package com.wolfmq.store.domain.timer.rocksdb;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.mock.ConfigMock;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class RocksdbTimerTest {

    @Test
    void addAndScan_ShouldReturnDefaults(@TempDir Path tempDir) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        RocksdbTimer timer = new RocksdbTimer(storeConfig);

        assertFalse(timer.addTimer(TimerEvent.builder().build()));
        assertNull(timer.scan(1000));
        assertDoesNotThrow(timer::recover);
    }
}

