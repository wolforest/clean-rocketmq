package com.wolfmq.store.domain.dispatcher;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.commitlog.log.CommitLogManager;
import com.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import com.wolfmq.store.server.bootstrap.StoreContext;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DispatcherBootstrapTest {

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
    void initialize_ShouldRegisterDispatchManagerAndHandlerManager(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        StoreContext.register(storeConfig);
        StoreContext.CHECK_POINT = new StoreCheckpoint(anotherDir.toString());

        TopicService topicService = mock(TopicService.class);
        StoreContext.register(topicService, TopicService.class);

        CommitLogManager commitLogManager = new CommitLogManager(
            storeConfig.getCommitConfig(),
            new TopicPartitioner(storeConfig.getCommitConfig(), topicService)
        );
        StoreContext.register(commitLogManager);

        DispatcherBootstrap bootstrap = new DispatcherBootstrap();
        bootstrap.initialize();

        DispatchManager dispatchManager = StoreContext.getBean(DispatchManager.class);
        assertNotNull(dispatchManager);

        CommitHandlerManager handlerManager = StoreContext.getBean(CommitHandlerManager.class);
        assertNotNull(handlerManager);
    }
}
