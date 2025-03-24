package cn.coderule.minimq.store.domain.mq;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.domain.service.store.domain.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.manager.MessageQueueManager;
import cn.coderule.minimq.domain.service.store.domain.MessageQueue;
import cn.coderule.minimq.store.api.MessageStoreImpl;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMessageQueueManager implements MessageQueueManager {
    @Override
    public void initialize() {
        MessageConfig messageConfig = StoreContext.getBean(MessageConfig.class);
        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);

        MessageQueue messageQueue = new DefaultMessageQueue(messageConfig, commitLog, consumeQueueGateway);
        StoreContext.register(messageQueue, MessageQueue.class);

        MessageStore messageStore = new MessageStoreImpl(messageConfig, messageQueue);
        StoreContext.registerAPI(messageStore, MessageStore.class);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
