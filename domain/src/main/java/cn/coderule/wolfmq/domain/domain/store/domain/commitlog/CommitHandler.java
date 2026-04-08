package cn.coderule.wolfmq.domain.domain.store.domain.commitlog;

/**
 * @renamed from CommitEventHandler to CommitHandler
 */
public interface CommitHandler {
    void handle(CommitEvent event);
}
