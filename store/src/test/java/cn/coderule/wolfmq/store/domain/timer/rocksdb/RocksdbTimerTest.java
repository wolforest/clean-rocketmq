package cn.coderule.wolfmq.store.domain.timer.rocksdb;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.test.ConfigMock;
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

