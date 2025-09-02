package cn.coderule.minimq.store.infra.file;

import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMappedFileQueueTest {

    @Test
    void testCreateMappedFileQueue(@TempDir Path tmpDir) {
        MappedFileQueue queue = new DefaultMappedFileQueue(tmpDir.toString(), 1024);
        queue.load();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());

        assertNull(queue.getFirstMappedFile());
        assertNull(queue.getLastMappedFile());
        assertNull(queue.getMappedFileByOffset(0));
        assertNotNull(queue.createMappedFileForSize(10));

        queue.destroy();
    }
}
