package cn.coderule.wolfmq.store.server.bootstrap;

import cn.coderule.wolfmq.store.infra.StoreScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreContextTest {

    @Mock
    private StoreScheduler mockScheduler;

    @Mock
    private StoreCheckpoint mockCheckpoint;

    @TempDir
    Path tempDir;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        StoreContext.SCHEDULER = null;
        StoreContext.CHECK_POINT = null;
        StoreContext.ISOLATED = false;
        StoreContext.STATE_MACHINE_VERSION = 0L;
        mocks.close();
    }

    @Test
    void testSetAndGetScheduler() {
        StoreContext.setScheduler(mockScheduler);
        
        assertEquals(mockScheduler, StoreContext.getScheduler());
    }

    @Test
    void testGetSchedulerThrowsWhenNull() {
        assertThrows(RuntimeException.class, StoreContext::getScheduler);
    }

    @Test
    void testSetCheckPoint() {
        StoreContext.setCheckPoint(mockCheckpoint);
        
        assertEquals(mockCheckpoint, StoreContext.getCheckPoint());
    }

    @Test
    void testSetCheckPointThrowsWhenAlreadySet() {
        StoreContext.setCheckPoint(mockCheckpoint);
        
        assertThrows(RuntimeException.class, () -> StoreContext.setCheckPoint(mockCheckpoint));
    }

    @Test
    void testIsolated() {
        assertFalse(StoreContext.isIsolated());
        
        StoreContext.setIsolated(true);
        
        assertTrue(StoreContext.isIsolated());
    }

    @Test
    void testStateMachineVersion() {
        assertEquals(0L, StoreContext.getStateMachineVersion());
        
        StoreContext.setStateMachineVersion(100L);
        
        assertEquals(100L, StoreContext.getStateMachineVersion());
    }

    @Test
    void testRegisterAndGetBean() {
        String testBean = "test";
        
        StoreContext.register(testBean, String.class);
        
        assertEquals(testBean, StoreContext.getBean(String.class));
    }

    @Test
    void testRegisterAPIAndGetAPI() {
        Integer apiBean = 42;
        
        StoreContext.registerAPI(apiBean, Integer.class);
        
        assertEquals(apiBean, StoreContext.getAPI(Integer.class));
    }

    @Test
    void testRegisterMonitorAndGetMonitor() {
        Double monitorBean = 3.14;
        
        StoreContext.registerMonitor(monitorBean, Double.class);
        
        assertEquals(monitorBean, StoreContext.getMonitor(Double.class));
    }
}
