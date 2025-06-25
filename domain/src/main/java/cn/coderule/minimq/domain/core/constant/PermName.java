package cn.coderule.minimq.domain.core.constant;

public class PermName {
    public static final int INDEX_PERM_PRIORITY = 3;
    public static final int INDEX_PERM_READ = 2;
    public static final int INDEX_PERM_WRITE = 1;
    public static final int INDEX_PERM_INHERIT = 0;


    public static final int PERM_PRIORITY = 0x1 << INDEX_PERM_PRIORITY;
    public static final int PERM_READ = 0x1 << INDEX_PERM_READ;
    public static final int PERM_WRITE = 0x1 << INDEX_PERM_WRITE;
    public static final int PERM_INHERIT = 0x1 << INDEX_PERM_INHERIT;

    public static String perm2String(final int perm) {
        final StringBuilder sb = new StringBuilder("---");
        if (isReadable(perm)) {
            sb.replace(0, 1, "R");
        }

        if (isWriteable(perm)) {
            sb.replace(1, 2, "W");
        }

        if (isInherited(perm)) {
            sb.replace(2, 3, "X");
        }

        return sb.toString();
    }

    public static boolean isReadable(final int perm) {
        return (perm & PERM_READ) == PERM_READ;
    }

    public static boolean isWriteable(final int perm) {
        return (perm & PERM_WRITE) == PERM_WRITE;
    }

    public static boolean isInherited(final int perm) {
        return (perm & PERM_INHERIT) == PERM_INHERIT;
    }

    public static boolean isValid(final String perm) {
        return isValid(Integer.parseInt(perm));
    }

    public static boolean isValid(final int perm) {
        return perm >= 0 && perm < PERM_PRIORITY;
    }

    public static boolean isPriority(final int perm) {
        return (perm & PERM_PRIORITY) == PERM_PRIORITY;
    }
}
