package cn.coderule.wolfmq.store.server.ha.core.hook;

public interface HAReadHook {
    void afterRead(int readSize);
}
