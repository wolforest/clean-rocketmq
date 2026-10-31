package com.wolfmq.broker.domain.timer;


import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.domain.producer.ProduceHookManager;
import com.wolfmq.broker.domain.timer.context.TimerContext;
import com.wolfmq.broker.domain.timer.service.TimerFactory;
import com.wolfmq.broker.domain.timer.transit.TimerMessageProducer;
import com.wolfmq.broker.domain.timer.transit.TimerTaskSaver;
import com.wolfmq.broker.domain.timer.transit.TimerTaskScanner;
import com.wolfmq.broker.domain.timer.transit.TimerTaskScheduler;
import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.broker.infra.store.TimerStore;
import com.wolfmq.domain.config.business.TimerConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.task.TaskLoader;
import com.wolfmq.domain.domain.timer.model.TimerQueue;
import com.wolfmq.domain.domain.timer.state.TimerState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public TimerBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private BrokerConfig brokerConfig;
    private TimerConfig timerConfig;

    private TimerContext timerContext;
    private TimerFactory timerFactory;

    private TimerTaskSaver taskSaver;
    private TimerTaskScanner taskScanner;

    private TimerMessageProducer[] messageProducers;
    private TimerTaskScheduler[] taskSchedulers;

    @Override
    public void initialize() throws Exception {
        brokerConfig = context.getBean(BrokerConfig.class);
        timerConfig = brokerConfig.getTimer();
        if (!timerConfig.isEnableTimer()) {
            return;
        }

        initTimerContext();

        taskSaver = new TimerTaskSaver(timerContext);
        taskScanner = new TimerTaskScanner(timerContext);
        initMessageProducers();
        initTaskSchedulers();

        initTimerFactory();
        injectTimerHook();
    }

    @Override
    public void start() throws Exception {
        if (!timerConfig.isEnableTimer()) {
            return;
        }
        timerContext.start();

        timerFactory.start();
        taskSaver.start();
        taskScanner.start();

        startMessageProducers();
        startTaskSchedulers();
    }

    @Override
    public void shutdown() throws Exception {
        if (!timerConfig.isEnableTimer()) {
            return;
        }

        timerFactory.shutdown();
        taskSaver.shutdown();
        taskScanner.shutdown();

        shutdownMessageProducers();
        shutdownTaskSchedulers();
    }

    private void initTimerContext() {
        int producerNum = timerConfig.getConsumerThreadNum();
        messageProducers = new TimerMessageProducer[producerNum];

        int schedulerNum = timerConfig.getSchedulerThreadNum();
        taskSchedulers = new TimerTaskScheduler[schedulerNum];

        timerContext = TimerContext.builder()
            .brokerConfig(brokerConfig)
            .timerQueue(new TimerQueue(timerConfig))
            .timerState(new TimerState(timerConfig))

            .messageProducers(messageProducers)
            .taskSchedulers(taskSchedulers)

            .mqStore(context.getBean(MQStore.class))
            .timerStore(context.getBean(TimerStore.class))

            .build();
    }

    private void initMessageProducers() {
        int producerNum = timerConfig.getConsumerThreadNum();
        for (int i = 0; i < producerNum; i++) {
            messageProducers[i] = new TimerMessageProducer(timerContext);
        }
    }

    private void initTaskSchedulers() {
        int schedulerNum = timerConfig.getSchedulerThreadNum();
        for (int i = 0; i < schedulerNum; i++) {
            taskSchedulers[i] = new TimerTaskScheduler(timerContext);
        }
    }

    private void startMessageProducers() throws Exception {
        for (TimerMessageProducer producer : messageProducers) {
            producer.start();
        }
    }

    private void startTaskSchedulers() throws Exception {
        for (TimerTaskScheduler scheduler : taskSchedulers) {
            scheduler.start();
        }
    }

    private void shutdownMessageProducers() throws Exception {
        for (TimerMessageProducer producer : messageProducers) {
            producer.shutdown();
        }
    }

    private void shutdownTaskSchedulers() throws Exception {
        for (TimerTaskScheduler scheduler : taskSchedulers) {
            scheduler.shutdown();
        }
    }

    private void initTimerFactory() {
        timerFactory = new TimerFactory(timerContext);

        TaskLoader loader = context.getBean(TaskLoader.class);
        loader.registerTimerFactory(timerFactory);
    }

    private void injectTimerHook() {
        // inject timer produce hook
        ProduceHookManager hookManager = context.getBean(ProduceHookManager.class);

        BrokerConfig brokerConfig = context.getBean(BrokerConfig.class);
        TimerHook hook = new TimerHook(brokerConfig);
        hookManager.registerHook(hook);
    }

}
