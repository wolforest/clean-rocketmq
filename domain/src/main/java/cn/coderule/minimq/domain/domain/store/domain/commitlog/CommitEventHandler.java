package cn.coderule.minimq.domain.domain.store.domain.commitlog;

public interface CommitEventHandler {
    void handle(CommitEvent event);
}
