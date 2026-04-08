package cn.coderule.wolfmq.store.domain.timer.service;

import cn.coderule.wolfmq.domain.config.business.TimerConfig;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.test.ConfigMock;
import cn.coderule.wolfmq.domain.test.MessageMock;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerServiceTest {

    @Test
    void whenTimerDisabled_ShouldUseBlackHole(@TempDir Path tempDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        storeConfig.getTimerConfig().setEnableTimer(false);

        CheckpointService checkpointService = new CheckpointService(tempDir.resolve("timercheck").toString());
        MessageService messageService = mock(MessageService.class);

        TimerService service = new TimerService(storeConfig, checkpointService, messageService);

        ScanResult result = service.scan(1000);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertFalse(service.addTimer(TimerEvent.builder().build()));

        service.flush();
    }

    @Test
    void whenRocksdbEnabled_ShouldUseRocksdbTimer(@TempDir Path tempDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        storeConfig.getTimerConfig().setEnableTimer(true);
        storeConfig.getTimerConfig().setEnableRocksDB(true);

        CheckpointService checkpointService = new CheckpointService(tempDir.resolve("timercheck").toString());
        MessageService messageService = mock(MessageService.class);

        TimerService service = new TimerService(storeConfig, checkpointService, messageService);
        assertNull(service.scan(1000));
        assertFalse(service.addTimer(TimerEvent.builder().build()));
    }

    @Test
    void whenDefaultTimerEnabled_ShouldAddAndScan(@TempDir Path tempDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        TimerConfig timerConfig = storeConfig.getTimerConfig();
        timerConfig.setEnableTimer(true);
        timerConfig.setEnableRocksDB(false);
        timerConfig.setTimerLogFileSize(1024);
        timerConfig.setTotalSlots(8);
        timerConfig.setWheelSlots(4);
        timerConfig.setPrecision(1000);

        StorePath.initPath(tempDir.toString());
        CheckpointService checkpointService = new CheckpointService(StorePath.getTimerCheckPath());
        MessageService messageService = mock(MessageService.class);

        TimerService service = new TimerService(storeConfig, checkpointService, messageService);

        long batchTime = System.currentTimeMillis();
        long delayTime = batchTime + 2000;
        MessageBO message = MessageMock.createMessage("timer-service", 128, 0, 0);
        message.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "timer-service");
        TimerEvent event = TimerEvent.builder()
            .batchTime(batchTime)
            .delayTime(delayTime)
            .commitLogOffset(888L)
            .messageSize(128)
            .messageBO(message)
            .build();

        assertTrue(service.addTimer(event));
        ScanResult result = service.scan(delayTime);
        assertTrue(result.isSuccess());
        assertEquals(1, result.sizeOfNormalMsgStack());

        service.flush();
    }
}

