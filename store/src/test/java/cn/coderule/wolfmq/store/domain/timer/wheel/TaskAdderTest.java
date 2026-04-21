package cn.coderule.wolfmq.store.domain.timer.wheel;

import cn.coderule.wolfmq.domain.config.business.TimerConfig;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.wheel.Slot;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.domain.mock.MessageMock;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class TaskAdderTest {

    @Test
    void addTimer_ShouldAppendAndUpdateWheel(@TempDir Path tempDir) throws Exception {
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

        long batchTime = System.currentTimeMillis();
        long delayTime = batchTime + 2000;
        MessageBO message = MessageMock.createMessage("timer-topic", 128, 0, 0);
        message.putProperty(MessageConst.PROPERTY_REAL_TOPIC, "timer-topic");
        TimerEvent event = TimerEvent.builder()
            .batchTime(batchTime)
            .delayTime(delayTime)
            .commitLogOffset(123L)
            .messageSize(128)
            .messageBO(message)
            .build();

        assertTrue(adder.addTimer(event));

        Slot slot = timerWheel.getSlot(delayTime);
        assertEquals(delayTime / timerConfig.getPrecision() * timerConfig.getPrecision(), slot.getTimeMs());
        assertEquals(1, slot.getNum());

        timerWheel.destroy();
        timerLog.destroy();
    }
}

