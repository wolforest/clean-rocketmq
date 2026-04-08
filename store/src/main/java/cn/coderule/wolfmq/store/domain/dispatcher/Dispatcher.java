package cn.coderule.wolfmq.store.domain.dispatcher;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @renamed from CommitDispatcher to Dispatcher
 * @renamed from CommitEventDispatcher to CommitDispatcher
 */
@Slf4j
public class Dispatcher extends ServiceThread {
    private final DispatchQueue queue;
    private final CommitHandlerManager handlerManager;

    public Dispatcher(DispatchQueue queue, CommitHandlerManager handlerManager) {
        this.queue = queue;
        this.handlerManager = handlerManager;
    }

    @Override
    public String getServiceName() {
        return Dispatcher.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            try {
                dispatch();
                ThreadUtil.sleep(1);
            } catch (Exception e) {
                log.error("{} service has exception. ", getServiceName(), e);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    private void dispatch() throws InterruptedException {
        CommitEvent event = queue.poll();
        if (event == null) {
            return;
        }

        handlerManager.handle(event);
    }
}
