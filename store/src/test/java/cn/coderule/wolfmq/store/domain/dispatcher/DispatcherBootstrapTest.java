package cn.coderule.wolfmq.store.domain.dispatcher;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
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