package com.wolfmq.store.domain.mq;

import com.wolfmq.domain.server.store.CheckPoint;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.MQStore;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.ConsumeOrderService;
import com.wolfmq.domain.domain.mq.MQService;
import com.wolfmq.domain.domain.consumer.consume.InflightCounter;
import com.wolfmq.domain.core.lock.queue.DequeueLock;
import com.wolfmq.domain.test.ConfigMock;
import com.wolfmq.store.domain.commitlog.CommitLogProxy;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueProxy;
import com.wolfmq.store.domain.meta.DefaultConsumeOffsetService;
import com.wolfmq.store.domain.meta.DefaultConsumeOrderService;
import com.wolfmq.store.domain.mq.ack.AckService;
import com.wolfmq.store.domain.mq.ack.InvisibleService;
import com.wolfmq.store.domain.mq.queue.DequeueService;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.domain.mq.queue.OffsetService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MQBootstrapTest {
    protected ApplicationContext context = new ApplicationContext();


    @TempDir
    Path tempDir;

    private StoreConfig storeConfig;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());

        storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        context.register(storeConfig);
        StoreCheckpoint __cp = new StoreCheckpoint(tempDir.toString()); context.register(__cp, CheckPoint.class);

        context.register(mock(CommitLogProxy.class), CommitLogProxy.class);
        context.register(mock(ConsumeQueueProxy.class), ConsumeQueueProxy.class);
        context.register(mock(ConsumeOffsetService.class), ConsumeOffsetService.class);
        context.register(mock(ConsumeOrderService.class), ConsumeOrderService.class);
        context.register(mock(DefaultConsumeOffsetService.class), DefaultConsumeOffsetService.class);
        context.register(mock(DefaultConsumeOrderService.class), DefaultConsumeOrderService.class);
        context.register(mock(CommitLog.class), CommitLog.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialize_ShouldRegisterAllBeans() throws Exception {
        MQBootstrap bootstrap = new MQBootstrap(context);
        bootstrap.initialize();

        assertNotNull(context.getBean(EnqueueService.class));
        assertNotNull(context.getBean(MessageService.class));
        assertNotNull(context.getBean(AckService.class));
        assertNotNull(context.getBean(InvisibleService.class));
        assertNotNull(context.getBean(OffsetService.class));
        assertNotNull(context.getBean(DequeueService.class));
        assertNotNull(context.getBean(MQService.class));
        assertNotNull(context.getBean(DequeueLock.class));
        assertNotNull(context.getBean(InflightCounter.class));
    }

    @Test
    void initialize_ShouldRegisterMQStoreAPI() throws Exception {
        MQBootstrap bootstrap = new MQBootstrap(context);
        bootstrap.initialize();

        assertNotNull(context.getBean(MQStore.class, ContextNamespace.API));
    }
}
