package cn.coderule.wolfmq.rpc.registry.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistryAdminTest {

    @Test
    void testConstructor() {
        RegistryAdmin admin = new RegistryAdmin();
        assertNotNull(admin);
    }
}