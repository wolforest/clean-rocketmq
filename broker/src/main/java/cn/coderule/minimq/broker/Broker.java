package cn.coderule.minimq.broker;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.broker.server.ComponentRegister;
import cn.coderule.minimq.broker.server.ContextInitializer;
import lombok.extern.slf4j.Slf4j;

/**
 * gateway of broker module
 *  - main()
 *  - command line argument process
 *  - broker start/shutdown process manager
 */
@Slf4j
public class Broker implements Lifecycle {

    /**
     * main entry of Broker
     * @param args command line argument
     */
    public static void main(String[] args) {
        new Broker(args).start();
    }

    private final String[] args;
    private State state = State.INITIALIZING;
    private LifecycleManager componentManager;

    public Broker(String[] args) {
        this.args = args;
    }

    /**
     * parse command line argument and initialize broker components
     * called by this.start()
     */
    @Override
    public void initialize() {
        ContextInitializer.init(args);
        this.componentManager = ComponentRegister.register();
        this.componentManager.initialize();
    }

    @Override
    public void start() {
        try {
            this.initialize();

            this.state = State.STARTING;
            this.componentManager.start();
            this.state = State.RUNNING;

            addShutdownHook();
        } catch (Exception e) {
            log.error("start broker error", e);
            System.exit(1);
        }

        log.info("Broker start successfully");
    }

    @Override
    public void shutdown() {
        log.info("Broker is shutting down ...");

        try {
            this.state = State.SHUTTING_DOWN;
            this.cleanup();

            this.componentManager.shutdown();

            this.state = State.TERMINATED;
        } catch (Exception e) {
            log.error("shutdown broker error", e);
        }

        log.info("Broker is terminated.");
    }

    @Override
    public void cleanup() {
        this.componentManager.cleanup();
    }

    @Override
    public State getState() {
        return state;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(
            new Thread(this::shutdown)
        );
    }
}
