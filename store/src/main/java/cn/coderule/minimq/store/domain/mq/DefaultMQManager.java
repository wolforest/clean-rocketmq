package cn.coderule.minimq.store.domain.mq;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.lock.queue.ConsumeQueueLock;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.manager.MQManager;
import cn.coderule.minimq.domain.service.store.domain.MQService;
import cn.coderule.minimq.store.api.MQStoreImpl;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.commitlog.CommitLogSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMQManager implements MQManager {
    private ConsumeQueueLock consumeQueueLock;

    @Override
    public void initialize() {
        MessageConfig messageConfig = StoreContext.getBean(MessageConfig.class);
        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);
        CommitLogSynchronizer commitLogSynchronizer = StoreContext.getBean(CommitLogSynchronizer.class);
        consumeQueueLock = new ConsumeQueueLock();

        MQService MQService = new DefaultMQService(messageConfig, commitLog, consumeQueueGateway, commitLogSynchronizer, consumeQueueLock);
        StoreContext.register(MQService, MQService.class);

        MQStore MQStore = new MQStoreImpl(messageConfig, MQService);
        StoreContext.registerAPI(MQStore, MQStore.class);
    }

    @Override
    public void start() {
        consumeQueueLock.start();
    }

    @Override
    public void shutdown() {
        consumeQueueLock.shutdown();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
