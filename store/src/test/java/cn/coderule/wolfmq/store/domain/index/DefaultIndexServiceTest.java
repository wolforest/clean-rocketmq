package cn.coderule.wolfmq.store.domain.index;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultIndexServiceTest {

    @Test
    void testImplementsIndexService() {
        DefaultIndexService service = new DefaultIndexService();
        
        assertTrue(service instanceof cn.coderule.wolfmq.domain.domain.store.domain.index.IndexService);
    }

    @Test
    void testConstructor() {
        DefaultIndexService service = new DefaultIndexService();
        
        assertNotNull(service);
    }
}