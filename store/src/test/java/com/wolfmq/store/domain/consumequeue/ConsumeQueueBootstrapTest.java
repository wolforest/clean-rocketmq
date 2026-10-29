package com.wolfmq.store.domain.consumequeue;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import com.wolfmq.store.server.bootstrap.StoreContext;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumeQueueBootstrapTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        StorePath.initPath(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        StoreContext.APPLICATION.getBeanMap().clear();
        StoreContext.API.getBeanMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void initialize_ShouldRegisterConsumeQueueManager(@TempDir Path anotherDir) {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());
        storeConfig.setCommitConfig(new CommitConfig());

        StoreContext.register(storeConfig);
        StoreContext.register(storeConfig.getCommitConfig(), CommitConfig.class);
        StoreContext.CHECK_POINT = new StoreCheckpoint(anotherDir.toString());

        TopicService topicService = mock(TopicService.class);
        StoreContext.register(topicService, TopicService.class);

        CommitHandlerManager handlerManager = new CommitHandlerManager();
        StoreContext.register(handlerManager);

        ConsumeQueueBootstrap bootstrap = new ConsumeQueueBootstrap();
        assertDoesNotThrow(bootstrap::initialize);

        ConsumeQueueManager manager = StoreContext.getBean(ConsumeQueueManager.class);
        assertNotNull(manager);
    }
}
