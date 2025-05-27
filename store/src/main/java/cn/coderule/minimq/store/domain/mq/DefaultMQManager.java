package cn.coderule.minimq.store.domain.mq;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.domain.service.store.domain.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.manager.MQManager;
import cn.coderule.minimq.domain.service.store.domain.MessageService;
import cn.coderule.minimq.store.api.MessageStoreImpl;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.commitlog.CommitLogSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMQManager implements MQManager {
    @Override
    public void initialize() {
        MessageConfig messageConfig = StoreContext.getBean(MessageConfig.class);
        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);
        CommitLogSynchronizer commitLogSynchronizer = StoreContext.getBean(CommitLogSynchronizer.class);

        MessageService messageService = new DefaultMessageService(messageConfig, commitLog, consumeQueueGateway, commitLogSynchronizer);
        StoreContext.register(messageService, MessageService.class);

        MessageStore messageStore = new MessageStoreImpl(messageConfig, messageService);
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
