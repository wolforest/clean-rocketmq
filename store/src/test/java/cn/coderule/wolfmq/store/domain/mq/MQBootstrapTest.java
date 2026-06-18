package cn.coderule.wolfmq.store.domain.mq;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.MQService;
import cn.coderule.wolfmq.domain.domain.consumer.consume.InflightCounter;
import cn.coderule.wolfmq.domain.core.lock.queue.DequeueLock;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.store.domain.mq.ack.AckService;
import cn.coderule.wolfmq.store.domain.mq.ack.InvisibleService;
import cn.coderule.wolfmq.store.domain.mq.queue.DequeueService;
import cn.coderule.wolfmq.store.domain.mq.queue.EnqueueService;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import cn.coderule.wolfmq.store.domain.mq.queue.OffsetService;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MQBootstrapTest {

    @TempDir
    Path tempDir;

    private StoreConfig storeConfig;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;

        storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        StoreContext.register(storeConfig);
        StoreContext.CHECK_POINT = new StoreCheckpoint(tempDir.toString());

        StoreContext.register(mock(CommitLogManager.class), CommitLogManager.class);
        StoreContext.register(mock(ConsumeQueueManager.class), ConsumeQueueManager.class);
        StoreContext.register(mock(ConsumeOffsetService.class), ConsumeOffsetService.class);
        StoreContext.register(mock(ConsumeOrderService.class), ConsumeOrderService.class);
        StoreContext.register(mock(CommitLog.class), CommitLog.class);
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void initialize_ShouldRegisterAllBeans() throws Exception {
        MQBootstrap bootstrap = new MQBootstrap();
        bootstrap.initialize();

        assertNotNull(StoreContext.getBean(EnqueueService.class));
        assertNotNull(StoreContext.getBean(MessageService.class));
        assertNotNull(StoreContext.getBean(AckService.class));
        assertNotNull(StoreContext.getBean(InvisibleService.class));
        assertNotNull(StoreContext.getBean(OffsetService.class));
        assertNotNull(StoreContext.getBean(DequeueService.class));
        assertNotNull(StoreContext.getBean(MQService.class));
        assertNotNull(StoreContext.getBean(DequeueLock.class));
        assertNotNull(StoreContext.getBean(InflightCounter.class));
    }

    @Test
    void initialize_ShouldRegisterMQStoreAPI() throws Exception {
        MQBootstrap bootstrap = new MQBootstrap();
        bootstrap.initialize();

        assertNotNull(StoreContext.getAPI(cn.coderule.wolfmq.domain.domain.store.api.MQStore.class));
    }
}