package com.wolfmq.store.domain.dispatcher;

import com.wolfmq.domain.server.store.CheckPoint;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.commitlog.log.CommitLogManager;
import com.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DispatcherBootstrapTest {
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
    void initialize_ShouldRegisterDispatchManagerAndHandlerManager(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        context.register(storeConfig);
        StoreCheckpoint __cp = new StoreCheckpoint(anotherDir.toString()); context.register(__cp, CheckPoint.class);

        TopicService topicService = mock(TopicService.class);
        context.register(topicService, TopicService.class);

        CommitLogManager commitLogManager = new CommitLogManager(
            storeConfig.getCommitConfig(),
            new TopicPartitioner(storeConfig.getCommitConfig(), topicService)
        );
        context.register(commitLogManager);

        DispatcherBootstrap bootstrap = new DispatcherBootstrap(context);
        bootstrap.initialize();

        DispatchManager dispatchManager = context.getBean(DispatchManager.class);
        assertNotNull(dispatchManager);

        CommitHandlerManager handlerManager = context.getBean(CommitHandlerManager.class);
        assertNotNull(handlerManager);
    }
}
