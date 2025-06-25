package cn.coderule.minimq.domain.core.lock.server;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.SystemUtil;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutdownLock {
    private final String filePath;
    private final File file;

    public ShutdownLock(String filePath) {
        this.filePath = filePath;
        this.file = new File(filePath);
    }

    public boolean isLocked() {
        return file.exists();
    }

    public void lock() {
        try {
            boolean result = file.createNewFile();
            log.info("{}{}", filePath, result ? " create OK" : " already exists");

            long pid = SystemUtil.getPID();
            if (pid < 0) {
                pid = 0;
            }
            FileUtil.writeString(Long.toString(pid), file);
        } catch (IOException e) {
            throw new cn.coderule.common.lang.exception.lang.IOException(e.getMessage());
        }
    }

    public void unlock() {
        boolean result = file.delete();
        log.info("{}{}", filePath, result ? " delete OK" : " delete Failed");
    }
}
