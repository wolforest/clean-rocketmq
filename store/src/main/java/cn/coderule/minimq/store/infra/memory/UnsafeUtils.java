package cn.coderule.minimq.store.infra.memory;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class UnsafeUtils {
    public static final Unsafe UNSAFE = getUnsafe();
    public static final int OS_PAGE_SIZE = 1024 * 4;
    public static final int UNSAFE_PAGE_SIZE = getPageSize();

    public static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception ignore) {

        }
        return null;
    }

    public static int getPageSize() {
        if (UNSAFE == null) {
            return OS_PAGE_SIZE;
        }
        return UNSAFE.pageSize();
    }
}
