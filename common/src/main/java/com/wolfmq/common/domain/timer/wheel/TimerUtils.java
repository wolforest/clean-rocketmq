package com.wolfmq.common.domain.timer.wheel;

import com.wolfmq.common.domain.timer.model.TimerConstants;

public class TimerUtils {
    public static boolean needDelete(int magic) {
        return (magic & TimerConstants.MAGIC_DELETE) != 0;
    }

    public static boolean needRoll(int magic) {
        return (magic & TimerConstants.MAGIC_ROLL) != 0;
    }

    public static boolean isMagicOK(int magic) {
        return (magic | 0xF) == 0xF;
    }

}
