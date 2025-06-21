package cn.coderule.minimq.broker.server.core;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import java.util.concurrent.ScheduledExecutorService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelScheduler implements Lifecycle {
    private final ScheduledExecutorService scheduler;

    public ChannelScheduler() {
        this.scheduler = ThreadUtil.newSingleScheduledThreadExecutor(
            new DefaultThreadFactory("channelScheduler-")
        );
    }

    @Override
    public void initialize() {
        ProducerController producerController = BrokerContext.getAPI(ProducerController.class);
        ConsumerController consumerController = BrokerContext.getAPI(ConsumerController.class);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
    }


}
