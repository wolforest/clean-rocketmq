package cn.coderule.minimq.domain.domain.constant.flag;

public class PullSysFlag {
    private final static int FLAG_COMMIT_OFFSET = 0x1;
    private final static int FLAG_SUSPEND = 0x1 << 1;
    private final static int FLAG_SUBSCRIPTION = 0x1 << 2;
    private final static int FLAG_CLASS_FILTER = 0x1 << 3;
    private final static int FLAG_LITE_PULL_MESSAGE = 0x1 << 4;

    public static int buildSysFlag(final boolean commitOffset, final boolean suspend,
        final boolean subscription, final boolean classFilter) {
        int flag = 0;

        if (commitOffset) {
            flag |= FLAG_COMMIT_OFFSET;
        }

        if (suspend) {
            flag |= FLAG_SUSPEND;
        }

        if (subscription) {
            flag |= FLAG_SUBSCRIPTION;
        }

        if (classFilter) {
            flag |= FLAG_CLASS_FILTER;
        }

        return flag;
    }

    public static int buildSysFlag(final boolean commitOffset, final boolean suspend,
        final boolean subscription, final boolean classFilter, final boolean litePull) {
        int flag = buildSysFlag(commitOffset, suspend, subscription, classFilter);

        if (litePull) {
            flag |= FLAG_LITE_PULL_MESSAGE;
        }

        return flag;
    }

    public static int clearCommitOffsetFlag(final int sysFlag) {
        return sysFlag & (~FLAG_COMMIT_OFFSET);
    }

    public static boolean hasCommitOffsetFlag(final int sysFlag) {
        return (sysFlag & FLAG_COMMIT_OFFSET) == FLAG_COMMIT_OFFSET;
    }

    public static boolean hasSuspendFlag(final int sysFlag) {
        return (sysFlag & FLAG_SUSPEND) == FLAG_SUSPEND;
    }

    public static int clearSuspendFlag(final int sysFlag) {
        return sysFlag & (~FLAG_SUSPEND);
    }

    public static boolean hasSubscriptionFlag(final int sysFlag) {
        return (sysFlag & FLAG_SUBSCRIPTION) == FLAG_SUBSCRIPTION;
    }

    public static int buildSysFlagWithSubscription(final int sysFlag) {
        return sysFlag | FLAG_SUBSCRIPTION;
    }

    public static boolean hasClassFilterFlag(final int sysFlag) {
        return (sysFlag & FLAG_CLASS_FILTER) == FLAG_CLASS_FILTER;
    }

    public static boolean hasLitePullFlag(final int sysFlag) {
        return (sysFlag & FLAG_LITE_PULL_MESSAGE) == FLAG_LITE_PULL_MESSAGE;
    }
}
