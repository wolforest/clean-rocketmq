package cn.coderule.minimq.store.infra.file;

import cn.coderule.minimq.domain.utils.StoreUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DefaultMappedFileTest {

    @TempDir
    Path tmpDir;

    private final AtomicLong counter = new AtomicLong(10000);

    @Test
    void testCreateMappedFile() {
        String fileName = createFileName(0);
        int fileSize = 2 * 1024;

        try {
            DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

            assertEquals(0, mappedFile.getMinOffset());
            assertEquals(fileSize, mappedFile.getMaxOffset());
            assertEquals(0, mappedFile.getInsertPosition());

            mappedFile.destroy(1000);
        } catch (IOException e) {
            log.error("create mappedFile exception", e);
        }
    }

    private String createFileName() {
        String fileName = StoreUtils.offsetToFileName(counter.getAndIncrement());

        return tmpDir.resolve(fileName).toString();
    }

    private String createFileName(int i) {
        String fileName = StoreUtils.offsetToFileName(i);

        return tmpDir.resolve(fileName).toString();
    }


}
