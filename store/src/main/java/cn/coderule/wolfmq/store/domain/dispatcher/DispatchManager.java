package cn.coderule.wolfmq.store.domain.dispatcher;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
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
    private final CommitLogManager commitLogManager;
    private final CommitHandlerManager handlerManager;

    private final List<CommitListener> listenerList = new ArrayList<>();
    private final List<Dispatcher> dispatcherList = new ArrayList<>();

    public DispatchManager(
        CommitConfig config,
        CheckPoint checkPoint,
        CommitLogManager commitLogManager,
        CommitHandlerManager handlerManager
    ) {
        this.config = config;
        this.checkPoint = checkPoint;
        this.commitLogManager = commitLogManager;
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
            CommitLog commitLog = commitLogManager.selectByShardId(i);
            CommitListener listener = new CommitListener(queue, commitLog, checkPoint);
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
