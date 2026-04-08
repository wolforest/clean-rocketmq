package cn.coderule.wolfmq.store.domain.timer.wheel;

import cn.coderule.wolfmq.domain.domain.timer.wheel.Slot;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class TimerWheelTest {

    @Test
    void testInsertAndSelect(@TempDir Path tempDir) throws Exception {
        TimerWheel wheel = new TimerWheel(tempDir.resolve("timerwheel").toString(), 4, 1000);

        long timeMs = 2000;
        wheel.putSlot(timeMs, 10L, 20L, 3, 7);

        Slot slot = wheel.getSlot(timeMs);
        assertEquals(2000, slot.getTimeMs());
        assertEquals(10L, slot.getFirstPos());
        assertEquals(20L, slot.getLastPos());
        assertEquals(3, slot.getNum());
        assertEquals(7, slot.getMagic());

        assertEquals(2, wheel.getSlotIndex(2000));
        assertEquals(3, wheel.getNum(timeMs));

        wheel.destroy();
    }

}
