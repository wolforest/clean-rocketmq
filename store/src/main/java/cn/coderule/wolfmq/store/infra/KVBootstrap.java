package cn.coderule.wolfmq.store.infra;

import cn.coderule.common.convention.service.Lifecycle;

public class KVBootstrap implements Lifecycle {
    private final KVStore kvStore = new KVStore();

    @Override
    public void initialize() throws Exception {

        kvStore.initialize();
    }

    @Override
    public void start() throws Exception {
       kvStore.start();
    }

    @Override
    public void shutdown() throws Exception {
        kvStore.shutdown();
    }
}
