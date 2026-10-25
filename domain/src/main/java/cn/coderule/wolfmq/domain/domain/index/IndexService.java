package cn.coderule.wolfmq.domain.domain.index;

public interface IndexService {

    void buildIndex(String topic, String keys, long phyOffset, long storeTimestamp);

    QueryOffsetResult queryOffset(String topic, String key, int maxNum, long begin, long end);

    boolean load(boolean lastExitOK);

    void start();

    void shutdown();

    void deleteExpiredFile(long offset);

    void destroy();

    long getTotalSize();
}
