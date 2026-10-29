package com.wolfmq.store.domain.mq.it;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.config.store.ConsumeQueueConfig;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.ConsumeOrderService;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.server.store.CheckPoint;
import com.wolfmq.domain.mock.ConfigMock;
import com.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import com.wolfmq.store.domain.commitlog.log.CommitLogManager;
import com.wolfmq.store.domain.commitlog.sharding.OffsetCodec;
import com.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueLoader;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import com.wolfmq.store.server.bootstrap.StoreContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseMQIntegrationTest {

    protected static final int MMAP_FILE_SIZE = 1024 * 1024;

    @TempDir
    protected Path tmpDir;

    protected StoreConfig storeConfig;
    protected AllocateMappedFileService allocateService;
    protected CommitLogManager commitLogManager;
    protected ConsumeQueueManager consumeQueueManager;
    protected MessageService messageService;
    protected EnqueueService enqueueService;
    protected ConsumeOffsetService consumeOffsetService;
    protected ConsumeOrderService consumeOrderService;

    @BeforeEach
    void setUpInfrastructure() throws Exception {
        resetStoreContext();

        String rootPath = tmpDir.toString();
        storeConfig = ConfigMock.createStoreConfig(rootPath);
        storeConfig.getCommitConfig().setFileSize(MMAP_FILE_SIZE);
        storeConfig.getCommitConfig().setShardingNumber(1);
        storeConfig.getCommitConfig().setMaxShardingNumber(1);

        StoreContext.register(storeConfig);
        StoreContext.register(storeConfig.getCommitConfig());

        initCheckPoint();

        allocateService = new AllocateMappedFileService(storeConfig);
        allocateService.start();
        StoreContext.register(allocateService);

        initCommitLog();
        initConsumeQueue();
        initMetaServices();
        initMQServices();
    }

    @AfterEach
    void tearDownInfrastructure() throws Exception {
        if (commitLogManager != null) {
            commitLogManager.shutdown();
        }
        if (allocateService != null) {
            allocateService.shutdown();
        }
        resetStoreContext();
    }

    private void resetStoreContext() {
        StoreContext.CHECK_POINT = null;
        StoreContext.APPLICATION.getBeanMap().clear();
        StoreContext.API.getBeanMap().clear();
    }

    private void initCheckPoint() {
        StoreCheckpoint checkpoint = new StoreCheckpoint(
            tmpDir.resolve("checkpoint").toString()
        );
        checkpoint.setShutdownSuccessful(false);
        StoreContext.setCheckPoint(checkpoint);
    }

    private void initCommitLog() throws Exception {
        CheckPoint checkpoint = StoreContext.getCheckPoint();
        CommitConfig commitConfig = storeConfig.getCommitConfig();

        CommitLogFactory factory = new CommitLogFactory(storeConfig, checkpoint);
        List<CommitLog> logList = factory.createByConfig();

        TopicPartitioner partitioner = new TopicPartitioner(commitConfig);
        commitLogManager = new CommitLogManager(commitConfig, partitioner);
        commitLogManager.addCommitLog(logList);

        StoreContext.register(partitioner);
        StoreContext.register(commitLogManager);

        commitLogManager.initialize();
        commitLogManager.start();

        CommitLog cl = commitLogManager.selectByShardId(0);
        cl.getMappedFileQueue().getOrCreateMappedFileForSize(100);
    }

    private void initConsumeQueue() {
        ConsumeQueueConfig cqConfig = storeConfig.getConsumeQueueConfig();
        StoreCheckpoint checkpoint = StoreContext.getCheckPoint();
        CommitConfig commitConfig = storeConfig.getCommitConfig();

        TopicService topicService = mock(TopicService.class);
        when(topicService.exists(anyString())).thenReturn(true);
        StoreContext.register(topicService, TopicService.class);

        ConsumeQueueFlusher flusher = new ConsumeQueueFlusher(cqConfig, checkpoint);
        ConsumeQueueLoader loader = new ConsumeQueueLoader(cqConfig);
        OffsetCodec offsetCodec = new OffsetCodec(commitConfig.getMaxShardingNumber());
        ConsumeQueueRecovery recovery = new ConsumeQueueRecovery(cqConfig, checkpoint, offsetCodec);

        ConsumeQueueFactory factory = new ConsumeQueueFactory(cqConfig, topicService, checkpoint);
        factory.addCreateHook(flusher);
        factory.addCreateHook(loader);
        factory.addCreateHook(recovery);
        factory.createAll();

        consumeQueueManager = new ConsumeQueueManager(factory);
        StoreContext.register(consumeQueueManager, ConsumeQueueManager.class);
    }

    private void initMetaServices() {
        consumeOffsetService = mock(ConsumeOffsetService.class);
        consumeOrderService = mock(ConsumeOrderService.class);
    }

    private void initMQServices() {
        messageService = new MessageService(commitLogManager, consumeQueueManager);

        enqueueService = new EnqueueService(storeConfig, commitLogManager, consumeQueueManager);
        StoreContext.register(enqueueService, EnqueueService.class);
        StoreContext.register(messageService, MessageService.class);
    }

    protected CommitLog getCommitLog() {
        return commitLogManager.selectByShardId(0);
    }
}
