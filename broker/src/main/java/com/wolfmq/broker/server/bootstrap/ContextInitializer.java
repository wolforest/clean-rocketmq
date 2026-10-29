package com.wolfmq.broker.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.container.EventBus;
import com.wolfmq.domain.core.constant.ContextNamespace;

/**
 * context initializer
 *  - parse command line arguments
 *  - load broker configuration
 *  - init broker libs
 */
public class ContextInitializer {
    private final String[] args;
    private final BrokerArgument argument;
    private final ApplicationContext context;

    public static void init(String[] args, ApplicationContext context) {
        ContextInitializer initializer = new ContextInitializer(args, context);
        initializer.initialize();
    }

    public ContextInitializer(String[] args, ApplicationContext context) {
        this.args = args;
        this.argument = new BrokerArgument();
        this.context = context;
    }

    public void initialize() {
        ConfigLoader.load(context);
        initLibs();
    }

    private void initLibs() {
        EventBus eventBus = new EventBus();
        context.register(eventBus, ContextNamespace.APP);
    }

}
