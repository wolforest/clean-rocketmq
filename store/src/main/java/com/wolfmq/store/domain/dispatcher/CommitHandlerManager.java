package com.wolfmq.store.domain.dispatcher;

import com.wolfmq.common.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.common.domain.commitlog.dispatch.CommitHandler;
import java.util.ArrayList;

/**
 * @renamed from CommitEventHandlerManager to CommitHandlerManager
 */
public class CommitHandlerManager implements CommitHandler {
    // question: list -> set ?
    private final ArrayList<CommitHandler> handlerList = new ArrayList<>();

    /**
     * register CommitHandler, and receive CommitEvent
     *
     * @renamed from registerHandler to register
     */
    public void register(CommitHandler handler) {
        handlerList.add(handler);
    }

    public boolean isEmpty() {
        return handlerList.isEmpty();
    }

    @Override
    public void handle(CommitEvent event) {
        if (handlerList.isEmpty()) return;

        for (CommitHandler handler : handlerList) {
            handler.handle(event);
        }
    }
}
