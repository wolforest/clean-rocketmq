package com.wolfmq.store.domain.dispatcher;

import com.wolfmq.domain.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.domain.domain.commitlog.dispatch.CommitHandler;
import java.util.ArrayList;

/**
 * @renamed from CommitEventHandlerManager to CommitHandlerManager
 */
public class CommitHandlerManager implements CommitHandler {
    private final ArrayList<CommitHandler> handlerList = new ArrayList<>();

    public void registerHandler(CommitHandler handler) {
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
