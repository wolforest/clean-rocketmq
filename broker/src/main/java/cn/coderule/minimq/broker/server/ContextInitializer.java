package cn.coderule.minimq.broker.server;

import cn.coderule.minimq.broker.server.model.BrokerArgument;
import cn.coderule.minimq.broker.server.model.BrokerContext;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.GrpcConfig;

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
        initializeConfig();
    }

    private void initializeConfig() {
        BrokerContext.register(new MessageConfig());
        BrokerContext.register(new GrpcConfig());
        BrokerContext.register(new BrokerConfig());
    }


}
