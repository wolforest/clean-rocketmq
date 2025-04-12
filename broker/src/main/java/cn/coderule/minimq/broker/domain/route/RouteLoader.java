package cn.coderule.minimq.broker.domain.route;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.broker.domain.route.model.RouteCache;
import cn.coderule.minimq.broker.infra.BrokerRegister;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.exception.RpcException;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteLoader implements Lifecycle {
    private final BrokerConfig brokerConfig;

    private final BrokerRegister brokerRegister;
    private final RouteCache route;

    private ScheduledExecutorService scheduler;

    public RouteLoader(BrokerConfig brokerConfig, RouteCache route, BrokerRegister brokerRegister) {
        this.brokerConfig = brokerConfig;

        this.route = route;
        this.brokerRegister = brokerRegister;

        this.scheduler = ThreadUtil.newSingleScheduledThreadExecutor(
            new DefaultThreadFactory("BrokerRouteScheduler")
        );
    }

    @Override
    public void start() {
        this.scheduler.scheduleAtFixedRate(
            RouteLoader.this::load,
            1000,
            brokerConfig.getSyncRouteInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void shutdown() {
        this.scheduler.shutdown();
    }

    public void updateRouteInfo(String topicName, boolean updatePubInfo, boolean updateSubInfo) {
        if (!route.tryLock()) {
            return;
        }

        try {

        } catch (RpcException e) {

        } catch (Exception e) {

        } finally {
            route.unlock();
        }
    }

    private void load() {
        try {
            Set<String> topicSet = route.getTopicSet();
            for (String topicName : topicSet) {
                boolean updatePubInfo = route.containsRoute(topicName);
                boolean updateSubInfo = route.containsSubscription(topicName);
                updateRouteInfo(topicName, updatePubInfo, updateSubInfo);
            }
        } catch (Exception e) {
            log.error("Load route info exception", e);
        }
    }


}
