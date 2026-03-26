package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.store.CommitConfig;

/**
 * @renamed from CommitDispatcher to Dispatcher
 * @renamed from CommitEventDispatcher to CommitDispatcher
 */
public class Dispatcher extends ServiceThread {
    private final CommitConfig config;
    private final DispatchQueue queue;
    private final CommitHandlerManager handlerManager;

    public Dispatcher(CommitConfig config, DispatchQueue queue, CommitHandlerManager handlerManager) {
        this.config = config;
        this.queue = queue;
        this.handlerManager = handlerManager;
    }

    @Override
    public String getServiceName() {
        return Dispatcher.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
