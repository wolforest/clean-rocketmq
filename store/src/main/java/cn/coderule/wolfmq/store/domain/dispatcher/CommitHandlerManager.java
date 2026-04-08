package cn.coderule.wolfmq.store.domain.dispatcher;

import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitHandler;
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
