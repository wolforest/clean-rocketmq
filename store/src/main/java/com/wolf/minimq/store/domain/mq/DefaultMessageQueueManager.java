package com.wolf.minimq.store.domain.mq;

import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.service.store.api.StoreService;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueueStore;
import com.wolf.minimq.domain.service.store.manager.MessageQueueManager;
import com.wolf.minimq.domain.service.store.domain.MessageStore;
import com.wolf.minimq.store.api.StoreServiceImpl;
import com.wolf.minimq.store.server.StoreContext;
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
