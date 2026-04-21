package cn.coderule.wolfmq.store.domain.timer.wheel;

import cn.coderule.wolfmq.domain.config.business.TimerConfig;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import cn.coderule.wolfmq.store.domain.timer.service.CheckpointService;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerRecoverTest {

    @Test
    void recover_ShouldNotThrow(@TempDir Path tempDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        TimerConfig timerConfig = storeConfig.getTimerConfig();
        timerConfig.setTimerLogFileSize(1024);
        timerConfig.setTotalSlots(8);
        timerConfig.setPrecision(1000);

        StorePath.initPath(tempDir.toString());
        TimerLog timerLog = new TimerLog(StorePath.getTimerLogPath(), timerConfig.getTimerLogFileSize());
        TimerWheel timerWheel = new TimerWheel(StorePath.getTimerWheelPath(), timerConfig.getTotalSlots(), timerConfig.getPrecision());
        CheckpointService checkpointService = new CheckpointService(StorePath.getTimerCheckPath());
        MessageService messageService = mock(MessageService.class);

        TimerRecover recover = new TimerRecover(storeConfig, timerLog, timerWheel, checkpointService, messageService);
        assertDoesNotThrow(recover::recover);

        timerWheel.destroy();
        timerLog.destroy();
    }
}

