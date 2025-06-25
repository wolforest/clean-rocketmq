package cn.coderule.minimq.domain.domain.core.constant.flag;

public class SubscriptionSysFlag {

    private final static int FLAG_UNIT = 0x1 << 0;

    public static int buildSysFlag(final boolean unit) {
        int sysFlag = 0;

        if (unit) {
            sysFlag |= FLAG_UNIT;
        }

        return sysFlag;
    }

    public static int setUnitFlag(final int sysFlag) {
        return sysFlag | FLAG_UNIT;
    }

    public static int clearUnitFlag(final int sysFlag) {
        return sysFlag & (~FLAG_UNIT);
    }

    public static boolean hasUnitFlag(final int sysFlag) {
        return (sysFlag & FLAG_UNIT) == FLAG_UNIT;
    }

    public static void main(String[] args) {
    }
}
