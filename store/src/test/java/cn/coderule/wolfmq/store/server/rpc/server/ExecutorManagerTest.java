package cn.coderule.wolfmq.store.server.rpc.server;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorManagerTest {

    private StoreConfig storeConfig;
    private ExecutorManager executorManager;

    @BeforeEach
    void setUp() {
        storeConfig = new StoreConfig();
        storeConfig.setAdminThreadNum(2);
        storeConfig.setAdminQueueCapacity(100);
        storeConfig.setPullThreadNum(4);
        storeConfig.setPullQueueCapacity(200);
        storeConfig.setEnqueueThreadNum(8);
        storeConfig.setEnqueueQueueCapacity(300);

        executorManager = new ExecutorManager(storeConfig);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (executorManager != null) {
            executorManager.shutdown();
        }
    }

    @Test
    void testConstructor() throws Exception {
        assertNotNull(executorManager);
        executorManager.initialize();

        assertNotNull(executorManager.getAdminExecutor());
        assertNotNull(executorManager.getPullExecutor());
        assertNotNull(executorManager.getEnqueueExecutor());
    }

    @Test
    void testInitialize() throws Exception {
        executorManager.initialize();

        assertNotNull(executorManager.getAdminExecutor());
        assertNotNull(executorManager.getPullExecutor());
        assertNotNull(executorManager.getEnqueueExecutor());
    }

    @Test
    void testStart() throws Exception {
        executorManager.initialize();
        assertDoesNotThrow(() -> executorManager.start());
    }

    @Test
    void testShutdown() throws Exception {
        executorManager.initialize();
        assertDoesNotThrow(() -> executorManager.shutdown());
    }

    @Test
    void testShutdownWithNullExecutors() {
        ExecutorManager manager = new ExecutorManager(storeConfig);
        assertDoesNotThrow(manager::shutdown);
    }
}
