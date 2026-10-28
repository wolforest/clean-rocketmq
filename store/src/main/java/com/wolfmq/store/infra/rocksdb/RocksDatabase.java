package com.wolfmq.store.infra.rocksdb;

import com.wolfmq.domain.config.store.KVConfig;
import com.wolfmq.domain.core.kv.KVDatabase;
import com.wolfmq.domain.core.kv.KVTable;
import lombok.Getter;
import org.rocksdb.RocksDB;

public class RocksDatabase implements KVDatabase {
    @Getter
    private final String name;
    private final KVConfig config;

    static {
         RocksDB.loadLibrary();
    }

    public RocksDatabase(KVConfig kvConfig) {
        this.name = kvConfig.getDbName();
        this.config = kvConfig;
    }

    @Override
    public KVTable openTable(String name) {
        return null;
    }

    @Override
    public boolean containsTable(String name) {
        return false;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void close() throws Exception {

    }
}
