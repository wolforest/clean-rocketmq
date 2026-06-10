package cn.coderule.wolfmq.store.server.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarmupServiceTest {

    @Test
    void getServiceName_ShouldReturnClassName() {
        WarmupService service = new WarmupService();
        assertEquals("WarmupService", service.getServiceName());
    }
}