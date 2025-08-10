package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.producer.ProduceHookManager;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;

public class TimerManager implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        // inject timer produce hook
        ProduceHookManager hookManager = BrokerContext.getBean(ProduceHookManager.class);
        TimerHook hook = new TimerHook();
        hookManager.registerHook(hook);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
