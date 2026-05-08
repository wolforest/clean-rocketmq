package cn.coderule.wolfmq.store.domain.consumequeue.it;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.ConsumeQueue;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueLoader;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.OffsetCodec;
import cn.coderule.wolfmq.store.infra.file.AllocateMappedFileService;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

public abstract class BaseConsumeQueueIntegrationTest {

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
        storeConfig.getCommitConfig().setFileSize(1024 * 1024);
        storeConfig.getCommitConfig().setShardingNumber(1);
        storeConfig.getCommitConfig().setMaxShardingNumber(1);

        ConsumeQueueConfig cqConfig = storeConfig.getConsumeQueueConfig();
        cqConfig.setFileSize(FILE_SIZE);
        cqConfig.setUnitSize(UNIT_SIZE);

        StoreContext.register(storeConfig);
        StoreContext.register(storeConfig.getCommitConfig());
        StoreContext.register(cqConfig);

        initCheckPoint();

        allocateService = new AllocateMappedFileService(storeConfig);
        allocateService.start();
        StoreContext.register(allocateService);

        topicService = mock(TopicService.class);
        when(topicService.exists(TEST_TOPIC)).thenReturn(true);
        Topic topic = Topic.builder()
            .topicName(TEST_TOPIC)
            .readQueueNums(4)
            .writeQueueNums(4)
            .build();
        when(topicService.getTopic(TEST_TOPIC)).thenReturn(topic);
        StoreContext.register(topicService, TopicService.class);

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

    private void initConsumeQueue() {
        ConsumeQueueConfig cqConfig = storeConfig.getConsumeQueueConfig();
        CommitConfig commitConfig = storeConfig.getCommitConfig();
        StoreCheckpoint checkpoint = StoreContext.getCheckPoint();

        flusher = new ConsumeQueueFlusher(cqConfig, checkpoint);
        loader = new ConsumeQueueLoader(cqConfig);

        OffsetCodec offsetCodec = new OffsetCodec(commitConfig.getMaxShardingNumber());
        recovery = new ConsumeQueueRecovery(cqConfig, checkpoint, offsetCodec);

        consumeQueueFactory = new ConsumeQueueFactory(cqConfig, topicService, checkpoint);
        consumeQueueFactory.addCreateHook(flusher);
        consumeQueueFactory.addCreateHook(loader);
        consumeQueueFactory.addCreateHook(recovery);
        consumeQueueFactory.createAll();

        consumeQueueManager = new ConsumeQueueManager(consumeQueueFactory);
        StoreContext.register(consumeQueueManager, ConsumeQueueManager.class);
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