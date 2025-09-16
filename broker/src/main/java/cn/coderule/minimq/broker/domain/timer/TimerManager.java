package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.producer.ProduceHookManager;
import cn.coderule.minimq.broker.domain.timer.service.TimerFactory;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class TimerManager implements Lifecycle {
    private TimerFactory timerFactory;

    @Override
    public void initialize() throws Exception {
        injectTimerHook();
        initTimerFactory();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    private void initTimerFactory() {

    }

    private void injectTimerHook() {
        // inject timer produce hook
        ProduceHookManager hookManager = BrokerContext.getBean(ProduceHookManager.class);

        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        TimerHook hook = new TimerHook(brokerConfig);
        hookManager.registerHook(hook);
    }

}
