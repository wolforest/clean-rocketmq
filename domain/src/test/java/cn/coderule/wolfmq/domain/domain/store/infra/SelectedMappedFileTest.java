package cn.coderule.wolfmq.domain.domain.store.infra;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SelectedMappedFileTest {

    @Test
    void testBuilder() {
        MappedFile mappedFile = mock(MappedFile.class);
        SelectedMappedFile selected = SelectedMappedFile.builder()
            .size(256)
            .mappedFile(mappedFile)
            .build();

        assertEquals(256, selected.getSize());
        assertEquals(mappedFile, selected.getMappedFile());
    }

    @Test
    void testSetters() {
        MappedFile mappedFile = mock(MappedFile.class);
        SelectedMappedFile selected = new SelectedMappedFile();
        selected.setSize(512);
        selected.setMappedFile(mappedFile);

        assertEquals(512, selected.getSize());
        assertEquals(mappedFile, selected.getMappedFile());
    }

    @Test
    void testNoArgsConstructor() {
        SelectedMappedFile selected = new SelectedMappedFile();
        assertNotNull(selected);
    }
}