package com.wolfmq.store.domain.dispatcher;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.offset.checkpoint.bakeup.CheckPoint;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage a group of CommitListener/Dispatcher
 *
 * @renamed from DispatcherManager to DispatchManager
 */
public class DispatchManager implements Lifecycle {
    private final CommitConfig config;

    private final CheckPoint checkPoint;
    private final DispatchQueue queue;
    private final CommitLogFacade commitLogFacade;
    private final CommitHandlerManager handlerManager;

    private final List<CommitListener> listenerList = new ArrayList<>();
    private final List<Dispatcher> dispatcherList = new ArrayList<>();

    public DispatchManager(
        CommitConfig config,
        CheckPoint checkPoint,
        CommitLogFacade commitLogFacade,
        CommitHandlerManager handlerManager
    ) {
        this.config = config;
        this.checkPoint = checkPoint;
        this.commitLogFacade = commitLogFacade;
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

        for (Dispatcher dispatcher : dispatcherList) {
            dispatcher.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (Dispatcher dispatcher : dispatcherList) {
            dispatcher.shutdown();
        }

        for (CommitListener listener : listenerList) {
            listener.shutdown();
        }
    }

    private void createListeners() {
        for (int i = 0; i < config.getShardingNumber(); i++) {
            CommitLog commitLog = commitLogFacade.selectByShardId(i);
            CommitListener listener = new CommitListener(queue, commitLog, checkPoint, config);
            listenerList.add(listener);
        }
    }

    private void createDispatchers() {
        for (int i = 0; i < config.getDispatchThreads(); i++) {
            Dispatcher dispatcher = new Dispatcher(queue, handlerManager);
            dispatcherList.add(dispatcher);
        }
    }

}
