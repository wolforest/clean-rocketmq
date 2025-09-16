package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.producer.ProduceHookManager;
import cn.coderule.minimq.broker.domain.timer.context.TimerContext;
import cn.coderule.minimq.broker.domain.timer.service.TimerFactory;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.infra.store.TimerStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;

public class TimerManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private TimerConfig timerConfig;

    private TimerFactory timerFactory;
    private TimerContext timerContext;

    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        timerConfig = brokerConfig.getTimerConfig();
        if (!timerConfig.isEnableTimer()) {
            return;
        }

        initTimerContext();
        initTimerFactory();

        injectTimerHook();
    }

    @Override
    public void start() throws Exception {
        if (!timerConfig.isEnableTimer()) {
            return;
        }
    }

    @Override
    public void shutdown() throws Exception {
        if (!timerConfig.isEnableTimer()) {
            return;
        }
    }

    private void initTimerContext() {
        timerContext = TimerContext.builder()
            .brokerConfig(brokerConfig)
            .timerQueue(new TimerQueue(timerConfig))
            .timerState(new TimerState(timerConfig))

            .mqStore(BrokerContext.getBean(MQStore.class))
            .timerStore(BrokerContext.getBean(TimerStore.class))

            .build();
    }

    private void initTimerFactory() {
        timerFactory = new TimerFactory(timerContext);
    }

    private void injectTimerHook() {
        // inject timer produce hook
        ProduceHookManager hookManager = BrokerContext.getBean(ProduceHookManager.class);

        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        TimerHook hook = new TimerHook(brokerConfig);
        hookManager.registerHook(hook);
    }

}
