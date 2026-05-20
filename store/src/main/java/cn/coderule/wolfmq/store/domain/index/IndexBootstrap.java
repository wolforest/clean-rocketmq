package cn.coderule.wolfmq.store.domain.index;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Bootstrap for the index module.
 *
 * Initializes index configuration, creates DefaultIndexService,
 * wires IndexCommitHandler, and registers with CommitHandlerManager.
 *
 * @renamed from IndexBootstrap
 */
@Slf4j
public class IndexBootstrap implements Lifecycle {
    private DefaultIndexService indexService;

    @Override
    public void initialize() throws Exception {
        initIndexService();
        registerCommitHandler();
    }

    @Override
    public void start() throws Exception {
        if (indexService != null) {
            indexService.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (indexService != null) {
            indexService.shutdown();
        }
    }

    private void initIndexService() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        indexService = new DefaultIndexService();
        StoreContext.register(indexService, DefaultIndexService.class);
    }

    private void registerCommitHandler() {
        CommitHandlerManager handlerManager = StoreContext.getBean(CommitHandlerManager.class);
        IndexCommitHandler handler = new IndexCommitHandler(indexService);
        handlerManager.registerHandler(handler);
    }
}
