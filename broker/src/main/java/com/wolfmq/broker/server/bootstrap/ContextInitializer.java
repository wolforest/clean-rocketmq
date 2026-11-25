package com.wolfmq.broker.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.container.EventBus;
import com.wolfmq.common.config.cli.BrokerConfigLoader;
import com.wolfmq.common.config.cli.StoreArgument;
import com.wolfmq.common.config.server.BrokerConfig;
import java.io.IOException;

/**
 * context initializer
 *  - parse command line arguments
 *  - load broker configuration
 *  - init broker libs
 */
public class ContextInitializer {
    private final ApplicationContext context;
    private final BrokerConfigLoader configLoader;

    public static void init(String[] args, ApplicationContext context) throws IOException {
        ContextInitializer initializer = new ContextInitializer(args, context);
        initializer.initialize();
    }

    public ContextInitializer(String[] args, ApplicationContext context) {
        this.context = context;
        this.configLoader = new BrokerConfigLoader(args);
    }

    public void initialize() throws IOException {
        BrokerConfig brokerConfig = configLoader.load();
        context.register(brokerConfig);

        StoreArgument storeArgument = configLoader.getArgument().toStoreArgument();
        context.register(storeArgument);

        initLibs();
    }

    private void initLibs() {
        EventBus eventBus = new EventBus();
        context.register(eventBus);
    }

}
