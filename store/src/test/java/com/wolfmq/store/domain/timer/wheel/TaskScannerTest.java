package com.wolfmq.store.domain.timer.wheel;

import com.wolfmq.common.config.business.TimerConfig;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.domain.message.property.MessageConst;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.timer.model.ScanResult;
import com.wolfmq.common.domain.timer.model.TimerEvent;
import com.wolfmq.common.testing.ConfigMock;
import com.wolfmq.common.testing.MessageMock;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class TaskScannerTest {

    @Test
    void scan_ShouldReturnEvent(@TempDir Path tempDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        TimerConfig timerConfig = storeConfig.getTimer();
        timerConfig.setTimerLogFileSize(1024);
        timerConfig.setTotalSlots(8);
        timerConfig.setWheelSlots(4);
        timerConfig.setPrecision(1000);

        TimerLog timerLog = new TimerLog(storeConfig.getPath().getTimerLogPath(), timerConfig.getTimerLogFileSize());
        TimerWheel timerWheel = new TimerWheel(storeConfig.getPath().getTimerWheelPath(), timerConfig.getTotalSlots(), timerConfig.getPrecision());

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

