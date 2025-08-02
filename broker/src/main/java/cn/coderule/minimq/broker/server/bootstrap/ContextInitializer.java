package cn.coderule.minimq.broker.server.bootstrap;

import cn.coderule.minimq.domain.service.common.ServerEventBus;

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
        ServerEventBus manager = new ServerEventBus();
        BrokerContext.register(manager);
    }

}
