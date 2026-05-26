package cn.coderule.wolfmq.domain.domain.store.infra;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectedMappedBufferTest {

    @Test
    void testBuilder() {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        SelectedMappedBuffer selected = SelectedMappedBuffer.builder()
            .startOffset(100L)
            .byteBuffer(buffer)
            .size(256)
            .isInCache(true)
            .build();

        assertEquals(100L, selected.getStartOffset());
        assertEquals(buffer, selected.getByteBuffer());
        assertEquals(256, selected.getSize());
        assertTrue(selected.isInCache());
    }

    @Test
    void testDefaultIsInCache() {
        SelectedMappedBuffer selected = new SelectedMappedBuffer();
        assertTrue(selected.isInCache());
    }

    @Test
    void testReleaseWithoutMappedFile() {
        SelectedMappedBuffer selected = new SelectedMappedBuffer();
        selected.release();
        assertTrue(selected.hasReleased());
    }

    @Test
    void testReleaseWithMappedFile() {
        MappedFile mappedFile = mock(MappedFile.class);
        SelectedMappedBuffer selected = SelectedMappedBuffer.builder()
            .mappedFile(mappedFile)
            .build();

        selected.release();
        assertTrue(selected.hasReleased());
        verify(mappedFile).release();
    }

    @Test
    void testIsInMemory() {
        SelectedMappedBuffer selected = new SelectedMappedBuffer();
        assertTrue(selected.isInMemory());
    }
}