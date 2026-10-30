package com.wolfmq.store.domain.index;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.store.domain.dispatcher.CommitHandlerManager;
import cn.coderule.common.convention.container.ApplicationContext;
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
    private final ApplicationContext context;

    private DefaultIndexService indexService;

    public IndexBootstrap(ApplicationContext context) {
        this.context = context;
    }

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
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        indexService = new DefaultIndexService(storeConfig);
        context.register(indexService, DefaultIndexService.class);
    }

    private void registerCommitHandler() {
        CommitHandlerManager handlerManager = context.getBean(CommitHandlerManager.class);
        IndexCommitHandler handler = new IndexCommitHandler(indexService);
        handlerManager.registerHandler(handler);
    }
}
