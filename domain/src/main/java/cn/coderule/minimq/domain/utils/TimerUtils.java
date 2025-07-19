package cn.coderule.minimq.domain.utils;

import cn.coderule.minimq.domain.domain.timer.TimerConstants;

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
