package cn.coderule.minimq.registry.server.bootstrap;

/**
 * ContextInitializer, parse command line args
 */
public class ContextInitializer {
    private final String[] args;

    public static void init(String[] args) {
        ContextInitializer initializer = new ContextInitializer(args);
        initializer.initialize();
    }

    public ContextInitializer(String[] args) {
        this.args = args;
    }

    public void initialize() {
    }

}
