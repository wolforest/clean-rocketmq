package cn.coderule.minimq.store.domain.mq;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.service.store.api.StoreService;
import cn.coderule.minimq.domain.service.store.domain.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueStore;
import cn.coderule.minimq.domain.service.store.manager.MessageQueueManager;
import cn.coderule.minimq.domain.service.store.domain.MessageStore;
import cn.coderule.minimq.store.api.StoreServiceImpl;
import cn.coderule.minimq.store.server.StoreContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMessageQueueManager implements MessageQueueManager {
    @Override
    public void initialize() {
        MessageConfig messageConfig = StoreContext.getBean(MessageConfig.class);
        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        ConsumeQueueStore consumeQueueStore = StoreContext.getBean(ConsumeQueueStore.class);

        MessageStore messageStore = new DefaultMessageStore(messageConfig, commitLog, consumeQueueStore);
        StoreContext.register(messageStore, MessageStore.class);

        StoreService storeService = new StoreServiceImpl(messageConfig, messageStore);
        StoreContext.registerAPI(storeService, StoreService.class);
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
