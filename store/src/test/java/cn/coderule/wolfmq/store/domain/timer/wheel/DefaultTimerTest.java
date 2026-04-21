package cn.coderule.wolfmq.store.domain.timer.wheel;

import cn.coderule.wolfmq.domain.config.business.TimerConfig;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.domain.mock.MessageMock;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import cn.coderule.wolfmq.store.domain.timer.service.CheckpointService;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultTimerTest {

    @Test
    void addAndScan_ShouldReturnEvent(@TempDir Path tempDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        TimerConfig timerConfig = storeConfig.getTimerConfig();
        timerConfig.setTimerLogFileSize(1024);
        timerConfig.setTotalSlots(8);
        timerConfig.setWheelSlots(4);
        timerConfig.setPrecision(1000);

        StorePath.initPath(tempDir.toString());
        CheckpointService checkpointService = new CheckpointService(StorePath.getTimerCheckPath());
        MessageService messageService = mock(MessageService.class);

        DefaultTimer timer = new DefaultTimer(storeConfig, checkpointService, messageService);
        timer.initialize();

        long batchTime = System.currentTimeMillis();
        long delayTime = batchTime + 2000;
        MessageBO message = MessageMock.createMessage("default-timer", 128, 0, 0);
        message.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "default-timer");
        TimerEvent event = TimerEvent.builder()
            .batchTime(batchTime)
            .delayTime(delayTime)
            .commitLogOffset(99L)
            .messageSize(128)
            .messageBO(message)
            .build();

        assertTrue(timer.addTimer(event));

        ScanResult result = timer.scan(delayTime);
        assertTrue(result.isSuccess());
        assertEquals(1, result.sizeOfNormalMsgStack());
        assertEquals(99L, result.getNormalMsgStack().getFirst().getCommitLogOffset());

        timer.flush();
        assertTrue(checkpointService.getCheckpoint().getLastTimerLogFlushPos() >= 0);

        timer.shutdown();
    }
}

