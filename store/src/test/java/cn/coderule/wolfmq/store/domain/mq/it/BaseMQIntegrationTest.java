package cn.coderule.wolfmq.store.domain.mq.it;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.OffsetCodec;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueLoader;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import cn.coderule.wolfmq.store.domain.mq.queue.EnqueueService;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import cn.coderule.wolfmq.store.infra.file.AllocateMappedFileService;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
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
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
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