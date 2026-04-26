package cn.coderule.wolfmq.store.server.ha.server.socket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReadServiceTest {

    @Test
    void testDefaultConstructor() {
        ReadService service = new ReadService();
        assertNotNull(service);
    }
}