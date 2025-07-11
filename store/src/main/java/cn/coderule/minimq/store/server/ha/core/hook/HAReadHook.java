package cn.coderule.minimq.store.server.ha.core.hook;

public interface HAReadHook {
    void afterRead(int readSize);
}
