package cn.coderule.minimq.store.infra.file;

import cn.coderule.minimq.domain.core.enums.store.InsertStatus;
import cn.coderule.minimq.domain.domain.cluster.store.InsertResult;
import cn.coderule.minimq.domain.domain.cluster.store.SelectedMappedBuffer;
import cn.coderule.minimq.domain.utils.store.OffsetUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DefaultMappedFileTest {

    @TempDir
    Path tmpDir;

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

    @Test
    void testInsertByteBuffer() {
        String fileName = createFileName(2);
        int fileSize = 20 * 100;

        try {
            DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);


            ByteBuffer buffer = createByteBuffer();
            mappedFile.insert(buffer);

            SelectedMappedBuffer result = mappedFile.select(0, 20);
            ByteBuffer resultBuffer = result.getByteBuffer();
            assertEquals(50, resultBuffer.getLong());
            assertEquals(30, resultBuffer.getInt());
            assertEquals(8L, resultBuffer.getLong());

            mappedFile.destroy(1000);
        } catch (IOException e) {
            log.error("create mappedFile exception", e);
        }
    }

    private ByteBuffer createByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(20);

        buffer.putLong(50);
        buffer.putInt(30);
        buffer.putLong(8L);

        buffer.flip();
        buffer.limit(20);

        return buffer;
    }

    @Test
    void testInsert() {
        String fileName = createFileName(100);
        int fileSize = 2 * 1024;

        try {
            DefaultMappedFile mappedFile = new DefaultMappedFile(fileName, fileSize);

            assertEquals(100, mappedFile.getMinOffset());
            assertEquals(fileSize + 100, mappedFile.getMaxOffset());
            assertEquals(0, mappedFile.getInsertPosition());

            byte[] data = "0123456789".getBytes();

            InsertResult insertResult = mappedFile.insert(data);
            assertEquals(InsertStatus.PUT_OK, insertResult.getStatus());
            assertEquals(0, insertResult.getWroteOffset());
            assertEquals(10, insertResult.getWroteBytes());
            assertEquals(10, mappedFile.getInsertPosition());


            insertResult = mappedFile.insert(data);
            assertEquals(InsertStatus.PUT_OK, insertResult.getStatus());
            assertEquals(10, insertResult.getWroteOffset());
            assertEquals(10, insertResult.getWroteBytes());
            assertEquals(20, mappedFile.getInsertPosition());

            mappedFile.destroy(1000);
        } catch (IOException e) {
            log.error("create mappedFile exception", e);
        }

    }

    private String createFileName(int i) {
        String fileName = OffsetUtils.offsetToFileName(i);

        return tmpDir.resolve(fileName).toString();
    }


}
