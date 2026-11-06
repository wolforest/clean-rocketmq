package com.wolfmq.store.domain.index.rocksdb;

import com.wolfmq.common.domain.index.IndexService;
import com.wolfmq.common.domain.index.QueryOffsetResult;

public class RocksdbIndexService implements IndexService {
    @Override
    public void buildIndex(String topic, String keys, long phyOffset, long storeTimestamp) {

    }

    @Override
    public QueryOffsetResult queryOffset(String topic, String key, int maxNum, long begin, long end) {
        return null;
    }

    @Override
    public boolean load(boolean lastExitOK) {
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void deleteExpiredFile(long offset) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public long getTotalSize() {
        return 0;
    }
}
