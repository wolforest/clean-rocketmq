package com.wolfmq.store.domain.mq.it;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.common.domain.meta.service.ConsumeOrderService;
import com.wolfmq.common.domain.meta.service.TopicService;
import com.wolfmq.common.server.store.checkpoint.bakeup.CheckPoint;
import com.wolfmq.common.testing.ConfigMock;
import com.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import com.wolfmq.common.domain.commitlog.route.OffsetCodec;
import com.wolfmq.store.domain.commitlog.route.TopicPartitioner;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFactory;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueFlusher;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueLoader;
import com.wolfmq.store.domain.consumequeue.file.ConsumeQueueRecovery;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import com.wolfmq.store.domain.mq.queue.MessageService;
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
    protected CommitLogFacade commitLogFacade;
    protected ConsumeQueueFacade consumeQueueFacade;
    protected MessageService messageService;
    protected EnqueueService enqueueService;
    protected ConsumeOffsetService consumeOffsetService;
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
        if (commitLogFacade != null) {
            commitLogFacade.shutdown();
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

    private void initCommitLog() throws Exception {
        CheckPoint checkpoint = context.getBean(CheckPoint.class);
        CommitConfig commitConfig = storeConfig.getCommit();

        CommitLogFactory factory = new CommitLogFactory(storeConfig, checkpoint, allocateService);
        List<CommitLog> logList = factory.createByConfig();

        TopicPartitioner partitioner = new TopicPartitioner(commitConfig);
        commitLogFacade = new CommitLogFacade(commitConfig, partitioner);
        commitLogFacade.addCommitLog(logList);

        context.register(partitioner);
        context.register(commitLogFacade);

        commitLogFacade.initialize();
        commitLogFacade.start();

        CommitLog cl = commitLogFacade.selectByShardId(0);
        cl.getMappedFileQueue().getOrCreateMappedFileForSize(100);
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

        ConsumeQueueFactory factory = new ConsumeQueueFactory(cqConfig, topicService, checkpoint);
        factory.addCreateHook(flusher);
        factory.addCreateHook(loader);
        factory.addCreateHook(recovery);
        factory.createAll();

        consumeQueueFacade = new ConsumeQueueFacade(factory);
        context.register(consumeQueueFacade, ConsumeQueueFacade.class);
    }

    private void initMetaServices() {
        consumeOffsetService = mock(ConsumeOffsetService.class);
        consumeOrderService = mock(ConsumeOrderService.class);
    }

    private void initMQServices() {
        messageService = new MessageService(commitLogFacade, consumeQueueFacade);

        enqueueService = new EnqueueService(storeConfig, commitLogFacade, consumeQueueFacade);
        context.register(enqueueService, EnqueueService.class);
        context.register(messageService, MessageService.class);
    }

    protected CommitLog getCommitLog() {
        return commitLogFacade.selectByShardId(0);
    }
}
