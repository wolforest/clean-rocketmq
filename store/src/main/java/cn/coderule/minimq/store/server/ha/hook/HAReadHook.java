package cn.coderule.minimq.store.server.ha.hook;

public interface HAReadHook {
    void afterRead(int readSize);
}
