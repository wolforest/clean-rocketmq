package cn.coderule.wolfmq.store.domain.consumequeue;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
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
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
        StoreContext.CHECK_POINT = null;
        StoreContext.SCHEDULER = null;
    }

    @Test
    void initialize_ShouldRegisterConsumeQueueManager(@TempDir Path anotherDir) throws Exception {
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
        assertDoesNotThrow(() -> bootstrap.initialize());

        ConsumeQueueManager manager = StoreContext.getBean(ConsumeQueueManager.class);
        assertNotNull(manager);
    }
}