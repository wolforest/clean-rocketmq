package com.wolfmq.store.domain.dispatcher;

import com.wolfmq.common.server.store.checkpoint.bakeup.CheckPoint;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.domain.meta.service.TopicService;
import com.wolfmq.common.testing.ConfigMock;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import com.wolfmq.store.domain.commitlog.route.TopicPartitioner;
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
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void initialize_ShouldRegisterDispatchManagerAndHandlerManager(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());

        context.register(storeConfig);
        StoreCheckpoint __cp = new StoreCheckpoint(anotherDir.toString()); context.register(__cp, CheckPoint.class);

        TopicService topicService = mock(TopicService.class);
        context.register(topicService, TopicService.class);

        CommitLogFacade commitLogFacade = new CommitLogFacade(
            storeConfig.getCommit(),
            new TopicPartitioner(storeConfig.getCommit(), topicService)
        );
        context.register(commitLogFacade);

        DispatcherBootstrap bootstrap = new DispatcherBootstrap(context);
        bootstrap.initialize();

        DispatchManager dispatchManager = context.getBean(DispatchManager.class);
        assertNotNull(dispatchManager);

        CommitHandlerManager handlerManager = context.getBean(CommitHandlerManager.class);
        assertNotNull(handlerManager);
    }
}
