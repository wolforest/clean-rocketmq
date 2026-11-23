package com.wolfmq.store.domain.dispatcher.dispatch;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import com.wolfmq.common.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @renamed from CommitEventDispatcher to CommitDispatcher
 * @renamed from CommitDispatcher to Dispatcher
 * @renamed from Dispatcher to CommitDispatcher
 */
@Slf4j
public class CommitDispatcher extends ServiceThread {
    private final DispatchQueue queue;
    private final CommitHandlerManager handlerManager;
    private final QueueProgress queueProgress;

    public CommitDispatcher(
        DispatchQueue queue,
        CommitHandlerManager handlerManager,
        QueueProgress queueProgress
    ) {
        this.queue = queue;
        this.handlerManager = handlerManager;
        this.queueProgress = queueProgress;
    }

    @Override
    public String getServiceName() {
        return CommitDispatcher.class.getSimpleName();
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
