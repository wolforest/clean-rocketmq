package cn.coderule.wolfmq.broker.domain.timer.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarmupServiceTest {

    @Test
    void getServiceName_ShouldReturnClassName() {
        WarmupService service = new WarmupService();
        assertEquals("WarmupService", service.getServiceName());
    }
}
