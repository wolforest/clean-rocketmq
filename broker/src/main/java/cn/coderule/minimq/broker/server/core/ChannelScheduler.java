package cn.coderule.minimq.broker.server.core;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelScheduler implements Lifecycle {
    private final ScheduledExecutorService scheduler;
    private final BrokerConfig brokerConfig;

    private ProducerController producerController;
    private ConsumerController consumerController;

    public ChannelScheduler(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.scheduler = ThreadUtil.newSingleScheduledThreadExecutor(
            new DefaultThreadFactory("channelScheduler-")
        );
    }

    @Override
    public void initialize() throws Exception {
        producerController = BrokerContext.getAPI(ProducerController.class);
        consumerController = BrokerContext.getAPI(ConsumerController.class);

        scheduler.scheduleAtFixedRate(
            this::scanIdleChannels,
            brokerConfig.getScanIdleChannelsDelay(),
            brokerConfig.getScanIdleChannelsInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    private void scanIdleChannels() {
        try {
            producerController.scanIdleChannels();
            consumerController.scanIdleChannels();
        } catch (Exception e) {
            log.error("ChannelScheduler#scanIdleChannels error", e);
        }
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        scheduler.shutdown();
    }


}
