package cn.coderule.wolfmq.store.server.ha.core.monitor;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FlowMonitorTest {

    private StoreConfig storeConfig;
    private FlowMonitor flowMonitor;

    @BeforeEach
    void setUp() {
        storeConfig = new StoreConfig();
        flowMonitor = new FlowMonitor(storeConfig);
    }

    @Test
    void testGetServiceName() {
        assertEquals("FlowMonitor", flowMonitor.getServiceName());
    }

    @Test
    void testAddTransferredByte() {
        flowMonitor.addTransferredByte(1024);
        flowMonitor.calculateSpeed();
        assertEquals(1024, flowMonitor.getTransferredBytePerSecond());
    }

    @Test
    void testCalculateSpeedResetsCounter() {
        flowMonitor.addTransferredByte(2048);
        flowMonitor.calculateSpeed();
        assertEquals(2048, flowMonitor.getTransferredBytePerSecond());

        flowMonitor.calculateSpeed();
        assertEquals(0, flowMonitor.getTransferredBytePerSecond());
    }

    @Test
    void testCalculateSpeedAccumulatesBytes() {
        flowMonitor.addTransferredByte(1024);
        flowMonitor.addTransferredByte(2048);
        flowMonitor.calculateSpeed();
        assertEquals(3072, flowMonitor.getTransferredBytePerSecond());
    }

    @Test
    void testGetAvailableTransferByteFlowControlDisabled() {
        assertFalse(storeConfig.isEnableHaFlowControl());
        assertEquals(Integer.MAX_VALUE, flowMonitor.getAvailableTransferByte());
    }

    @Test
    void testGetAvailableTransferByteFlowControlEnabled() {
        storeConfig.setEnableHaFlowControl(true);
        storeConfig.setMaxHaTransferBytesPerSecond(10_000);

        FlowMonitor monitor = new FlowMonitor(storeConfig);
        monitor.addTransferredByte(5000);

        int available = monitor.getAvailableTransferByte();
        assertTrue(available > 0);
        assertTrue(available <= 10_000);
    }

    @Test
    void testGetAvailableTransferByteExhausted() {
        storeConfig.setEnableHaFlowControl(true);
        storeConfig.setMaxHaTransferBytesPerSecond(100);

        FlowMonitor monitor = new FlowMonitor(storeConfig);
        monitor.addTransferredByte(200);

        assertEquals(0, monitor.getAvailableTransferByte());
    }

    @Test
    void testGetMaxTransferBytePerSecond() {
        storeConfig.setMaxHaTransferBytesPerSecond(50_000_000);
        assertEquals(50_000_000, flowMonitor.getMaxTransferBytePerSecond());
    }

    @Test
    void testIsEnableFlowControl() {
        assertFalse(flowMonitor.isEnableFlowControl());
        storeConfig.setEnableHaFlowControl(true);
        assertTrue(flowMonitor.isEnableFlowControl());
    }

    }