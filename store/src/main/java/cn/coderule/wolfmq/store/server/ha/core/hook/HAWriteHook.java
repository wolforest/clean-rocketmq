package cn.coderule.wolfmq.store.server.ha.core.hook;

public interface HAWriteHook {
    void afterWrite(int writeSize);
}
