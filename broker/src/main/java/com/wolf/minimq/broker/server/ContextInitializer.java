package com.wolf.minimq.broker.server;

import com.wolf.minimq.broker.server.model.BrokerArgument;
import com.wolf.minimq.domain.config.BrokerConfig;
import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.config.GrpcConfig;
import com.wolf.minimq.store.server.StoreContext;

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
        StoreContext.register(new MessageConfig());
        StoreContext.register(new GrpcConfig());
        StoreContext.register(new BrokerConfig());
    }


}
