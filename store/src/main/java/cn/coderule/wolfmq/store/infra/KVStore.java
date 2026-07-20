package cn.coderule.wolfmq.store.infra;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.domain.store.infra.kv.KVDatabase;
import cn.coderule.wolfmq.domain.domain.store.infra.kv.KVException;
import java.util.HashMap;
import java.util.Map;

public class KVStore implements Lifecycle {
    private final Map<String, KVDatabase> databases = new HashMap<>();

    public void register(KVDatabase database) {
        databases.put(database.getName(), database);
    }

    /**
     * KVStore.get("ConsumeQueue").openTable("queue");
     *
     * @return KVDatabase
     * @throws KVException if dbName not exists
     */
    public KVDatabase get(String dbName) throws KVException {
        return null;
    }

    @Override
    public void start() throws Exception {
        if (databases.isEmpty()) return;

        for (KVDatabase database : databases.values()) {
            database.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (databases.isEmpty()) return;

        for (KVDatabase database : databases.values()) {
            database.shutdown();
        }
    }

    @Override
    public void initialize() throws Exception {
        if (databases.isEmpty()) return;

        for (KVDatabase database : databases.values()) {
            database.initialize();
        }
    }
}
