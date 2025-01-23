package com.wolf.minimq.broker.server;

import com.wolf.minimq.broker.server.vo.BrokerArgument;
import com.wolf.minimq.domain.config.BrokerConfig;
import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.config.NetworkConfig;
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
        StoreContext.register(new NetworkConfig());
        StoreContext.register(new BrokerConfig());
    }


}
