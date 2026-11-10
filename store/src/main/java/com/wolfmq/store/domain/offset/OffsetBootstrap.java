package com.wolfmq.store.domain.offset;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.core.constant.ContextNamespace;
import com.wolfmq.common.domain.offset.ConsumerOffsetService;
import com.wolfmq.common.domain.offset.DefaultQueueProgress;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.common.domain.offset.checkpoint.CheckpointManager;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import com.wolfmq.common.domain.offset.consumequeue.QueueOffsetAssigner;
import com.wolfmq.common.domain.offset.consumer.DefaultConsumerOffsetService;
import com.wolfmq.common.server.store.StoreScheduler;
import com.wolfmq.common.server.store.StoreState;
import com.wolfmq.common.server.store.api.ConsumeOffsetStore;
import com.wolfmq.store.api.ConsumeOffsetStoreImpl;

public class OffsetBootstrap implements Lifecycle {
    private final ApplicationContext context;
    private final StoreConfig storeConfig;
    private final CheckpointManager checkpointManager;
    private final QueueOffsetAssigner queueOffsetAssigner;

    private ConsumerOffsetService offsetService;

    public OffsetBootstrap(ApplicationContext context) {
        this.context = context;
        this.storeConfig = context.getBean(StoreConfig.class);

        checkpointManager = new CheckpointManager(
            storeConfig,
            context.getBean(StoreScheduler.class)
        );

        queueOffsetAssigner = new QueueOffsetAssigner(
            checkpointManager.getCheckpointStore()
        );
    }

    @Override
    public void initialize() throws Exception {
        checkpointManager.initialize();
        queueOffsetAssigner.initialize();

        registerQueueProgress();

        registerConsumerOffset();
        offsetService.load();
    }

    @Override
    public void start() throws Exception {
        checkpointManager.start();
        queueOffsetAssigner.start();
    }

    @Override
    public void shutdown() throws Exception {
        checkpointManager.shutdown();
        queueOffsetAssigner.shutdown();
        offsetService.store();
    }

    private void registerConsumerOffset() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        offsetService = new DefaultConsumerOffsetService(storeConfig, storeConfig.getPath().getConsumerOffsetPath());
        context.register(offsetService, ConsumerOffsetService.class);

        ConsumeOffsetStore offsetStore = new ConsumeOffsetStoreImpl(offsetService);
        context.register(offsetStore, ConsumeOffsetStore.class, ContextNamespace.API);
        context.register(offsetStore, ConsumeOffsetStore.class);
    }

    private void registerQueueProgress() {
        int maxNumber = storeConfig.getCommit().getMaxShardingNumber();
        OffsetCodec offsetCodec = new OffsetCodec(maxNumber);

        DefaultQueueProgress queueProgress = new DefaultQueueProgress(
            checkpointManager.getCheckpointStore(),
            offsetCodec,
            queueOffsetAssigner
        );

        StoreState storeState = context.getBean(StoreState.class);
        queueProgress.setShutdownSuccessfully(storeState.isShutdownSuccessfully());

        context.register(queueProgress, QueueProgress.class);
    }


}
