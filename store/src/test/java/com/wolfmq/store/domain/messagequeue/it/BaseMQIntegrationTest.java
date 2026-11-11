package com.wolfmq.store.domain.messagequeue.it;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.offset.ConsumerOffsetService;
import com.wolfmq.common.domain.meta.service.ConsumeOrderService;
import com.wolfmq.common.domain.meta.service.TopicService;
import com.wolfmq.common.domain.offset.checkpoint.bakeup.CheckPoint;
import com.wolfmq.common.testing.ConfigMock;
import com.wolfmq.store.domain.commitlog.file.CommitLogFactory;
import com.wolfmq.store.domain.commitlog.file.FileCommitLog;
import com.wolfmq.store.domain.commitlog.file.CommitLogManager;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.common.domain.offset.DefaultQueueProgress;
import com.wolfmq.common.domain.offset.checkpoint.store.FileCheckpointStore;
import com.wolfmq.common.domain.offset.consumequeue.QueueOffsetAssigner;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import com.wolfmq.store.domain.commitlog.route.TopicPartitioner;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFactory;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueFlusher;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueLoader;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueRecovery;
import com.wolfmq.store.domain.messagequeue.queue.EnqueueService;
import com.wolfmq.store.domain.messagequeue.queue.MessageService;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseMQIntegrationTest {
    protected ApplicationContext context = new ApplicationContext();


    protected static final int MMAP_FILE_SIZE = 1024 * 1024;

    @TempDir
    protected Path tmpDir;

    protected StoreConfig storeConfig;
    protected AllocateMappedFileService allocateService;
    protected CommitLogManager commitLogManager;
    protected CommitLogFacade commitLogFacade;
    protected ConsumeQueueFacade consumeQueueFacade;
    protected MessageService messageService;
    protected EnqueueService enqueueService;
    protected ConsumerOffsetService consumerOffsetService;
    protected ConsumeOrderService consumeOrderService;

    @BeforeEach
    void setUpInfrastructure() throws Exception {
        resetStoreContext();

        String rootPath = tmpDir.toString();
        storeConfig = ConfigMock.createStoreConfig(rootPath);
        storeConfig.getCommit().setFileSize(MMAP_FILE_SIZE);
        storeConfig.getCommit().setShardingNumber(1);
        storeConfig.getCommit().setMaxShardingNumber(1);

        context.register(storeConfig);
        context.register(storeConfig.getCommit());

        initCheckPoint();

        allocateService = new AllocateMappedFileService(storeConfig);
        allocateService.start();
        context.register(allocateService);

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
    }

    private void initCheckPoint() {
        StoreCheckpoint checkpoint = new StoreCheckpoint(
            tmpDir.resolve("checkpoint").toString()
        );
        checkpoint.setShutdownSuccessful(false);
        context.register(checkpoint, CheckPoint.class);
    }

    private void initCommitLog() {
        QueueProgress queueProgress = createQueueProgress();
        CommitConfig commitConfig = storeConfig.getCommit();

        CommitLogFactory factory = new CommitLogFactory(storeConfig, queueProgress, allocateService);

        TopicPartitioner partitioner = new TopicPartitioner(commitConfig);
        commitLogManager = new CommitLogManager(commitConfig, factory);
        commitLogFacade = new CommitLogFacade(commitLogManager);

        context.register(partitioner);
        context.register(commitLogManager);
        context.register(commitLogFacade);

        try {
            commitLogManager.initialize();
            commitLogManager.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        ensureMappedFileExists();
    }

    private void ensureMappedFileExists() {
        CommitLog cl = commitLogManager.selectByShard(0);
        ((FileCommitLog) cl).getMappedFileQueue().getOrCreateMappedFileForSize(100);
    }

    private QueueProgress createQueueProgress() {
        FileCheckpointStore store = new FileCheckpointStore(
            tmpDir.resolve("queue-progress").toString()
        );

        OffsetCodec offsetCodec = new OffsetCodec(storeConfig.getCommit().getMaxShardingNumber());
        QueueOffsetAssigner assigner = new QueueOffsetAssigner(store);

        return new DefaultQueueProgress(store, offsetCodec, assigner);
    }

    private void initConsumeQueue() {
        ConsumeQueueConfig cqConfig = storeConfig.getConsumeQueue();
        CheckPoint checkpoint = context.getBean(CheckPoint.class);
        CommitConfig commitConfig = storeConfig.getCommit();

        TopicService topicService = mock(TopicService.class);
        when(topicService.exists(anyString())).thenReturn(true);
        context.register(topicService, TopicService.class);

        ConsumeQueueFlusher flusher = new ConsumeQueueFlusher(cqConfig);
        ConsumeQueueLoader loader = new ConsumeQueueLoader(cqConfig);
        OffsetCodec offsetCodec = new OffsetCodec(commitConfig.getMaxShardingNumber());
        ConsumeQueueRecovery recovery = new ConsumeQueueRecovery(cqConfig, checkpoint);

        ConsumeQueueFactory factory = new ConsumeQueueFactory(storeConfig, cqConfig, topicService, checkpoint);
        factory.addCreateHook(flusher);
        factory.addCreateHook(loader);
        factory.addCreateHook(recovery);
        factory.createAll();

        consumeQueueFacade = new ConsumeQueueFacade(factory);
        context.register(consumeQueueFacade, ConsumeQueueFacade.class);
    }

    private void initMetaServices() {
        consumerOffsetService = mock(ConsumerOffsetService.class);
        consumeOrderService = mock(ConsumeOrderService.class);
    }

    private void initMQServices() {
        messageService = new MessageService(commitLogFacade, consumeQueueFacade);

        enqueueService = new EnqueueService(storeConfig, commitLogFacade, consumeQueueFacade);
        context.register(enqueueService, EnqueueService.class);
        context.register(messageService, MessageService.class);
    }

    protected CommitLog getCommitLog() {
        return commitLogManager.selectByShard(0);
    }
}
