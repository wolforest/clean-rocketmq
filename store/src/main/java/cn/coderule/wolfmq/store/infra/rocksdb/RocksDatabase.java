package cn.coderule.wolfmq.store.infra.rocksdb;

import cn.coderule.wolfmq.domain.config.store.KVConfig;
import cn.coderule.wolfmq.domain.domain.store.infra.kv.KVDatabase;
import cn.coderule.wolfmq.domain.domain.store.infra.kv.KVTable;
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
