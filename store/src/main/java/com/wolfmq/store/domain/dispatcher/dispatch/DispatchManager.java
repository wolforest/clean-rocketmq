package com.wolfmq.store.domain.dispatcher.dispatch;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.store.domain.commitlog.file.CommitLogManager;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage a group of CommitListener/Dispatcher
 *
 * @renamed from DispatcherManager to DispatchManager
 */
public class DispatchManager implements Lifecycle {
    private final CommitConfig config;

    private final QueueProgress queueProgress;
    private final DispatchQueue queue;
    private final CommitLogManager manager;
    private final CommitHandlerManager handlerManager;

    private final List<CommitListener> listenerList = new ArrayList<>();
    private final List<CommitDispatcher> dispatcherList = new ArrayList<>();

    public DispatchManager(
        CommitConfig config,
        QueueProgress queueProgress,
        CommitLogManager manager,
        CommitHandlerManager handlerManager
    ) {
        this.config = config;
        this.queueProgress = queueProgress;
        this.manager = manager;
        this.handlerManager = handlerManager;
        this.queue = new DispatchQueue(config);
    }

    @Override
    public void initialize() throws Exception {
        createListeners();
        createDispatchers();
    }

    @Override
    public void start() throws Exception {
        for (CommitListener listener : listenerList) {
            listener.start();
        }

        for (CommitDispatcher dispatcher : dispatcherList) {
            dispatcher.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (CommitDispatcher dispatcher : dispatcherList) {
            dispatcher.shutdown();
        }

        for (CommitListener listener : listenerList) {
            listener.shutdown();
        }
    }

    private void createListeners() {
        for (int i = 0; i < config.getShardingNumber(); i++) {
            CommitLog commitLog = manager.selectByShard(i);
            CommitListener listener = new CommitListener(queue, commitLog, queueProgress, config);
            listenerList.add(listener);
        }
    }

    private void createDispatchers() {
        for (int i = 0; i < config.getDispatchThreads(); i++) {
            CommitDispatcher dispatcher = new CommitDispatcher(queue, handlerManager, queueProgress);
            dispatcherList.add(dispatcher);
        }
    }

}
