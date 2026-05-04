package cn.coderule.wolfmq.domain.domain.store.utils;

import cn.coderule.wolfmq.domain.domain.timer.TimerConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerUtilsTest {

    @Test
    void needDelete_withMagicDelete_returnsTrue() {
        assertTrue(TimerUtils.needDelete(TimerConstants.MAGIC_DELETE));
    }

    @Test
    void needDelete_withZero_returnsFalse() {
        assertFalse(TimerUtils.needDelete(0));
    }

    @Test
    void needDelete_withCombinedFlags_returnsTrue() {
        assertTrue(TimerUtils.needDelete(TimerConstants.MAGIC_DEFAULT | TimerConstants.MAGIC_DELETE));
    }

    @Test
    void needDelete_withRollOnly_returnsFalse() {
        assertFalse(TimerUtils.needDelete(TimerConstants.MAGIC_ROLL));
    }

    @Test
    void needRoll_withMagicRoll_returnsTrue() {
        assertTrue(TimerUtils.needRoll(TimerConstants.MAGIC_ROLL));
    }

    @Test
    void needRoll_withZero_returnsFalse() {
        assertFalse(TimerUtils.needRoll(0));
    }

    @Test
    void needRoll_withCombinedFlags_returnsTrue() {
        assertTrue(TimerUtils.needRoll(TimerConstants.MAGIC_DEFAULT | TimerConstants.MAGIC_ROLL));
    }

    @Test
    void needRoll_withDeleteOnly_returnsFalse() {
        assertFalse(TimerUtils.needRoll(TimerConstants.MAGIC_DELETE));
    }

    @Test
    void isMagicOK_withZero_returnsTrue() {
        assertTrue(TimerUtils.isMagicOK(0));
    }

    @Test
    void isMagicOK_withValuesOneToFifteen_returnsTrue() {
        for (int i = 1; i <= 15; i++) {
            assertTrue(TimerUtils.isMagicOK(i), "isMagicOK should return true for " + i);
        }
    }

    @Test
    void isMagicOK_withSixteenAndAbove_returnsFalse() {
        assertFalse(TimerUtils.isMagicOK(16));
        assertFalse(TimerUtils.isMagicOK(17));
        assertFalse(TimerUtils.isMagicOK(100));
    }

    @Test
    void isMagicOK_withMagicDefault_returnsTrue() {
        assertTrue(TimerUtils.isMagicOK(TimerConstants.MAGIC_DEFAULT));
    }

    @Test
    void isMagicOK_withMagicRoll_returnsTrue() {
        assertTrue(TimerUtils.isMagicOK(TimerConstants.MAGIC_ROLL));
    }

    @Test
    void isMagicOK_withMagicDelete_returnsTrue() {
        assertTrue(TimerUtils.isMagicOK(TimerConstants.MAGIC_DELETE));
    }
}