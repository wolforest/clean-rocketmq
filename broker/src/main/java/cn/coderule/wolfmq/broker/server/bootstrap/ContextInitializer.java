package cn.coderule.wolfmq.broker.server.bootstrap;

import cn.coderule.common.convention.container.EventBus;

/**
 * context initializer
 *  - parse command line arguments
 *  - load broker configuration
 *  - init broker libs
 */
public class ContextInitializer {
    private final String[] args;
    private final BrokerArgument argument;

    public static void init(String[] args) {
        ContextInitializer initializer = new ContextInitializer(args);
        initializer.initialize();
    }

    public ContextInitializer(String[] args) {
        this.args = args;
        this.argument = new BrokerArgument();
    }

    public void initialize() {
        ConfigLoader.load();
        initLibs();
    }

    private void initLibs() {
        EventBus eventBus = new EventBus();
        BrokerContext.register(eventBus);
    }

}
