package com.wolf.minimq.domain.utils.lock;

import com.wolf.common.util.io.FileUtil;
import com.wolf.common.util.lang.SystemUtil;
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
            FileUtil.writeToFile(Long.toString(pid), file);
        } catch (IOException e) {
            throw new com.wolf.common.lang.exception.io.IOException(e.getMessage());
        }
    }

    public void unlock() {
        boolean result = file.delete();
        log.info("{}{}", filePath, result ? " delete OK" : " delete Failed");
    }
}
