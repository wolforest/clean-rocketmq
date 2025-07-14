package cn.coderule.minimq.registry;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.registry.server.ComponentRegister;
import cn.coderule.minimq.registry.server.bootstrap.ConfigLoader;
import cn.coderule.minimq.registry.server.bootstrap.ContextInitializer;
import lombok.extern.slf4j.Slf4j;

/**
 * The entry point of the registry, registry is stateless.
 * Registry is component-based,
 * the start flow is:
 * -> main() -> start() -> initialize()
 * -> componentManager.start()
 * -> addShutdownHook()
 * the shutdown flow is:
 * -> shutdown() -> cleanup()
 * -> componentManager.shutdown()
 */
@Slf4j
public class Registry implements Lifecycle {
    public static void main(String[] args) {
        try {
            new Registry(args).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final String[] args;
    private LifecycleManager componentManager;

    public Registry(String[] args) {
        this.args = args;
    }

    @Override
    public void initialize() throws Exception {
        ContextInitializer.init(args);
        ConfigLoader.load();

        this.componentManager = ComponentRegister.register();
        this.componentManager.initialize();
    }

    @Override
    public void start() throws Exception {
        try {
            this.initialize();
            this.componentManager.start();
            addShutdownHook();
        } catch (Exception e) {
            log.error("start Registry error", e);
            System.exit(1);
        }

        log.info("Registry start successfully");
    }

    @Override
    public void shutdown() throws Exception {
        log.info("Registry is shutting down ...");

        try {
            this.cleanup();
            this.componentManager.shutdown();

        } catch (Exception e) {
            log.error("shutdown Registry error", e);
        }

        log.info("Registry is terminated.");
    }

    @Override
    public void cleanup() throws Exception {
        this.componentManager.cleanup();
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> {
                try {
                    shutdown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
        );
    }

}
