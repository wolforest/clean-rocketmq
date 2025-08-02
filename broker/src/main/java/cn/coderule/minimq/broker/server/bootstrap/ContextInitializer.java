package cn.coderule.minimq.broker.server.bootstrap;

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
    }

}
