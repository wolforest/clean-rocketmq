package cn.coderule.wolfmq.store.server.rpc;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.domain.store.api.TimerStore;
import cn.coderule.wolfmq.domain.domain.store.api.meta.ConsumeOffsetStore;
import cn.coderule.wolfmq.domain.domain.store.api.meta.SubscriptionStore;
import cn.coderule.wolfmq.domain.domain.store.api.meta.TopicStore;
import cn.coderule.wolfmq.store.domain.mq.queue.DequeueService;
import cn.coderule.wolfmq.store.domain.mq.queue.EnqueueService;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RpcBootstrapTest {

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
    void initialize_ShouldCreateServerAndProcessors(@TempDir Path anotherDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(anotherDir.toString());
        StorePath.initPath(anotherDir.toString());

        StoreContext.register(storeConfig);
        StoreContext.register(mock(TopicStore.class), TopicStore.class);
        StoreContext.register(mock(SubscriptionStore.class), SubscriptionStore.class);
        StoreContext.register(mock(ConsumeOffsetStore.class), ConsumeOffsetStore.class);
        StoreContext.register(mock(DequeueService.class), DequeueService.class);
        StoreContext.register(mock(EnqueueService.class), EnqueueService.class);
        StoreContext.register(mock(TimerStore.class), TimerStore.class);

        RpcBootstrap bootstrap = new RpcBootstrap();
        assertDoesNotThrow(() -> bootstrap.initialize());
    }
}