package cn.coderule.minimq.store.infra.memory;

import cn.coderule.common.util.io.BufferUtil;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

@Slf4j
public class MappedByteBufferUtils {
    /**
     * Check mapped file is loaded to memory with given position and size
     */
    private static final Method IS_LOADED_METHOD;

    static {
        Method isLoaded0method = null;
        // On the windows platform and openjdk 11 method isLoaded0 always returns false.
        // see https://github.com/AdoptOpenJDK/openjdk-jdk11/blob/19fb8f93c59dfd791f62d41f332db9e306bc1422/src/java.base/windows/native/libnio/MappedByteBuffer.c#L34
        if (!SystemUtils.IS_OS_WINDOWS) {
            try {
                isLoaded0method = MappedByteBuffer.class.getDeclaredMethod("isLoaded0", long.class, long.class, int.class);
                isLoaded0method.setAccessible(true);
            } catch (NoSuchMethodException ignore) {
            }
        }
        IS_LOADED_METHOD = isLoaded0method;
    }

    public boolean isLoaded(MappedByteBuffer mappedByteBuffer, long offset, long size) {
        if (IS_LOADED_METHOD == null) {
            return true;
        }
        try {
            long addr = BufferUtil.directBufferAddress(mappedByteBuffer) + offset;
            return (boolean) IS_LOADED_METHOD.invoke(mappedByteBuffer, mappingAddr(addr), size, pageCount(size));
        } catch (Exception e) {
            log.info("invoke isLoaded0 of file error:",  e);
        }
        return true;
    }

    public static long mappingAddr(long addr) {
        long offset = addr % UnsafeUtils.UNSAFE_PAGE_SIZE;
        offset = (offset >= 0) ? offset : (UnsafeUtils.UNSAFE_PAGE_SIZE + offset);
        return addr - offset;
    }

    public static int pageCount(long size) {
        return (int) (size + (long) UnsafeUtils.UNSAFE_PAGE_SIZE - 1L) / UnsafeUtils.UNSAFE_PAGE_SIZE;
    }


}
