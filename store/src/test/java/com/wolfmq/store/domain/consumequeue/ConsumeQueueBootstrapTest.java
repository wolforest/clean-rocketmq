package com.wolfmq.store.domain.consumequeue;

import com.wolfmq.domain.server.store.CheckPoint;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumeQueueBootstrapTest {
    protected ApplicationContext context = new ApplicationContext();


    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialize_ShouldRegisterConsumeQueueManager(@TempDir Path anotherDir) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());
        storeConfig.setCommitConfig(new CommitConfig());

        context.register(storeConfig);
        context.register(storeConfig.getCommitConfig(), CommitConfig.class);
        StoreCheckpoint __cp = new StoreCheckpoint(anotherDir.toString()); context.register(__cp, CheckPoint.class);

        TopicService topicService = mock(TopicService.class);
        context.register(topicService, TopicService.class);

        CommitHandlerManager handlerManager = new CommitHandlerManager();
        context.register(handlerManager);

        ConsumeQueueBootstrap bootstrap = new ConsumeQueueBootstrap(context);
        assertDoesNotThrow(bootstrap::initialize);

        ConsumeQueueManager manager = context.getBean(ConsumeQueueManager.class);
        assertNotNull(manager);
    }
}
