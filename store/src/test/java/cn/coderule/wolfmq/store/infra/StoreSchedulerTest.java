package cn.coderule.wolfmq.store.infra;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class StoreSchedulerTest {

    private StoreScheduler scheduler;

    @BeforeEach
    void setUp() {
        StoreConfig storeConfig = new StoreConfig();
        scheduler = new StoreScheduler(storeConfig);
    }

    @Test
    void testScheduleWithDelay() {
        Runnable task = () -> {};
        ScheduledFuture<?> future = scheduler.schedule(task, 100);
        assertNotNull(future);
        future.cancel(false);
    }

    @Test
    void testScheduleWithDelayAndTimeUnit() {
        Runnable task = () -> {};
        ScheduledFuture<?> future = scheduler.schedule(task, 1, TimeUnit.SECONDS);
        assertNotNull(future);
        future.cancel(false);
    }

    @Test
    void testScheduleAtFixedRate() {
        Runnable task = () -> {};
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 0, 100);
        assertNotNull(future);
        future.cancel(false);
    }

    @Test
    void testScheduleAtFixedRateWithTimeUnit() {
        Runnable task = () -> {};
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        assertNotNull(future);
        future.cancel(false);
    }

    @Test
    void testScheduleWithFixedDelay() {
        Runnable task = () -> {};
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, 0, 100);
        assertNotNull(future);
        future.cancel(false);
    }

    @Test
    void testScheduleWithFixedDelayAndTimeUnit() {
        Runnable task = () -> {};
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
        assertNotNull(future);
        future.cancel(false);
    }

    @Test
    void testShutdownDoesNotThrow() {
        assertDoesNotThrow(() -> scheduler.shutdown());
    }
}
