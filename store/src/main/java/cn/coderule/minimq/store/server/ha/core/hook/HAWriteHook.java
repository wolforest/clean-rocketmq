package cn.coderule.minimq.store.server.ha.core.hook;

public interface HAWriteHook {
    void afterWrite(int writeSize);
}
