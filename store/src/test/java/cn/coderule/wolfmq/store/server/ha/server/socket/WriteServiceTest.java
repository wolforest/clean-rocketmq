package cn.coderule.wolfmq.store.server.ha.server.socket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WriteServiceTest {

    @Test
    void testDefaultConstructor() {
        WriteService service = new WriteService();
        assertNotNull(service);
    }
}