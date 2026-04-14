package cn.coderule.wolfmq.store.server.ha.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WakeupCoordinatorTest {

    @Test
    void testConstructor() {
        WakeupCoordinator coordinator = new WakeupCoordinator();
        assertNotNull(coordinator);
    }

    @Test
    void testWakeup() {
        WakeupCoordinator coordinator = new WakeupCoordinator();
        assertDoesNotThrow(coordinator::wakeup);
    }

    @Test
    void testWakeupAll() {
        WakeupCoordinator coordinator = new WakeupCoordinator();
        assertDoesNotThrow(coordinator::wakeupAll);
    }

    @Test
    void testAwait() {
        WakeupCoordinator coordinator = new WakeupCoordinator();
        assertDoesNotThrow(() -> coordinator.await(100));
    }

    @Test
    void testAwaitAll() {
        WakeupCoordinator coordinator = new WakeupCoordinator();
        assertDoesNotThrow(() -> coordinator.awaitAll(100));
    }

    @Test
    void testRemoveCurrentThread() {
        WakeupCoordinator coordinator = new WakeupCoordinator();
        assertDoesNotThrow(coordinator::removeCurrentThread);
    }

    @Test
    void testWakeupAndAwait() throws InterruptedException {
        WakeupCoordinator coordinator = new WakeupCoordinator();

        Thread thread = new Thread(() -> coordinator.await(1000));
        thread.start();
        Thread.sleep(50);

        coordinator.wakeup();
        thread.join(2000);

        assertFalse(thread.isAlive());
    }
}
