package com.wolfmq.store.domain.consumequeue.it;

import com.wolfmq.domain.server.store.CheckPoint;


import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.config.store.ConsumeQueueConfig;
import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.domain.domain.consumequeue.ConsumeQueue;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.test.ConfigMock;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueLoader;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import com.wolfmq.store.domain.commitlog.sharding.OffsetCodec;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

public abstract class BaseConsumeQueueIntegrationTest {
    protected ApplicationContext context = new ApplicationContext();


    protected static final String TEST_TOPIC = "TestTopic";
    protected static final int TEST_QUEUE_ID = 0;
    protected static final int FILE_SIZE = 300_000 * 20;
    protected static final int UNIT_SIZE = 20;

    @TempDir
    protected Path tmpDir;

    protected StoreConfig storeConfig;
    protected AllocateMappedFileService allocateService;
    protected ConsumeQueueFactory consumeQueueFactory;
    protected ConsumeQueueManager consumeQueueManager;
    protected ConsumeQueueFlusher flusher;
    protected ConsumeQueueLoader loader;
    protected ConsumeQueueRecovery recovery;
    protected TopicService topicService;

    @BeforeEach
    void setUpInfrastructure() throws Exception {
        resetStoreContext();

        String rootPath = tmpDir.toString();
        storeConfig = ConfigMock.createStoreConfig(rootPath);
        storeConfig.getCommit().setFileSize(1024 * 1024);
        storeConfig.getCommit().setShardingNumber(1);
        storeConfig.getCommit().setMaxShardingNumber(1);

        ConsumeQueueConfig cqConfig = storeConfig.getConsumeQueue();
        cqConfig.setFileSize(FILE_SIZE);
        cqConfig.setUnitSize(UNIT_SIZE);

        context.register(storeConfig);
        context.register(storeConfig.getCommit());
        context.register(cqConfig);

        initCheckPoint();

        allocateService = new AllocateMappedFileService(storeConfig);
        allocateService.start();
        context.register(allocateService);

        topicService = mock(TopicService.class);
        when(topicService.exists(TEST_TOPIC)).thenReturn(true);
        Topic topic = Topic.builder()
            .topicName(TEST_TOPIC)
            .readQueueNums(4)
            .writeQueueNums(4)
            .build();
        when(topicService.getTopic(TEST_TOPIC)).thenReturn(topic);
        context.register(topicService, TopicService.class);

        initConsumeQueue();
    }

    @AfterEach
    void tearDownInfrastructure() throws Exception {
        if (flusher != null) {
            flusher.shutdown();
        }
        if (allocateService != null) {
            allocateService.shutdown();
        }
        destroyConsumeQueues();
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

    private void initConsumeQueue() {
        ConsumeQueueConfig cqConfig = storeConfig.getConsumeQueue();
        CommitConfig commitConfig = storeConfig.getCommit();
        CheckPoint checkpoint = context.getBean(CheckPoint.class);

        flusher = new ConsumeQueueFlusher(cqConfig);
        loader = new ConsumeQueueLoader(cqConfig);

        OffsetCodec offsetCodec = new OffsetCodec(commitConfig.getMaxShardingNumber());
        recovery = new ConsumeQueueRecovery(cqConfig, checkpoint, offsetCodec);

        consumeQueueFactory = new ConsumeQueueFactory(cqConfig, topicService, checkpoint);
        consumeQueueFactory.addCreateHook(flusher);
        consumeQueueFactory.addCreateHook(loader);
        consumeQueueFactory.addCreateHook(recovery);
        consumeQueueFactory.createAll();

        consumeQueueManager = new ConsumeQueueManager(consumeQueueFactory);
        context.register(consumeQueueManager, ConsumeQueueManager.class);
    }

    private void destroyConsumeQueues() {
        ConsumeQueue queue = consumeQueueFactory.get(TEST_TOPIC, TEST_QUEUE_ID);
        if (queue != null) {
            try {
                queue.destroy();
            } catch (Exception ignore) {
            }
        }
    }
}
