package com.wolfmq.store.domain.timer.wheel;

import com.wolfmq.domain.config.business.TimerConfig;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.core.constant.MessageConst;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.domain.mock.MessageMock;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class TaskScannerTest {

    @Test
    void scan_ShouldReturnEvent(@TempDir Path tempDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        TimerConfig timerConfig = storeConfig.getTimerConfig();
        timerConfig.setTimerLogFileSize(1024);
        timerConfig.setTotalSlots(8);
        timerConfig.setWheelSlots(4);
        timerConfig.setPrecision(1000);

        StorePath.initPath(tempDir.toString());
        TimerLog timerLog = new TimerLog(StorePath.getTimerLogPath(), timerConfig.getTimerLogFileSize());
        TimerWheel timerWheel = new TimerWheel(StorePath.getTimerWheelPath(), timerConfig.getTotalSlots(), timerConfig.getPrecision());

        TaskAdder adder = new TaskAdder(storeConfig, timerLog, timerWheel);
        TaskScanner scanner = new TaskScanner(storeConfig, timerLog, timerWheel);

        long batchTime = System.currentTimeMillis();
        long delayTime = batchTime + 2000;
        MessageBO message = MessageMock.createMessage("scan-topic", 128, 0, 0);
        message.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "scan-topic");
        TimerEvent event = TimerEvent.builder()
            .batchTime(batchTime)
            .delayTime(delayTime)
            .commitLogOffset(777L)
            .messageSize(128)
            .messageBO(message)
            .build();

        assertTrue(adder.addTimer(event));

        ScanResult result = scanner.scan(delayTime);
        assertTrue(result.isSuccess());
        assertEquals(1, result.sizeOfNormalMsgStack());
        TimerEvent scanned = result.getNormalMsgStack().getFirst();
        assertEquals(777L, scanned.getCommitLogOffset());
        assertEquals(delayTime, scanned.getDelayTime());

        timerWheel.destroy();
        timerLog.destroy();
    }
}

