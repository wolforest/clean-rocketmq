package cn.coderule.minimq.store.server.ha.hook;

public interface HAWriteHook {
    void afterWrite(int writeSize);
}
