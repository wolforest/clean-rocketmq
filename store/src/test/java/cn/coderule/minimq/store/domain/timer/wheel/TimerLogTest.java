package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.domain.store.infra.SelectedMappedBuffer;
import cn.coderule.minimq.domain.domain.timer.wheel.Block;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class TimerLogTest {

    @Test
    void testAppendAndGet(@TempDir Path tempDir) {
        TimerLog timerLog = new TimerLog(tempDir.resolve("timerlog").toString(), 1024);

        Block block = Block.builder()
            .size(Block.SIZE)
            .prevPos(-1)
            .magic(123)
            .currWriteTime(1000L)
            .delayedTime(2000)
            .offsetPy(456L)
            .sizePy(789)
            .hashCodeOfRealTopic(42)
            .reservedValue(0)
            .build();

        long offset = timerLog.append(block, 0, Block.SIZE);
        assertTrue(offset >= 0);

        SelectedMappedBuffer buffer = timerLog.getTimerMessage(offset);
        assertNotNull(buffer);
        assertEquals(offset, buffer.getStartOffset());

        buffer.release();
    }

}
