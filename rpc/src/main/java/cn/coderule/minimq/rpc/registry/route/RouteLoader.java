package cn.coderule.minimq.rpc.registry.route;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.core.exception.RpcException;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.utils.NamespaceUtils;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.domain.domain.cluster.route.PublishInfo;
import cn.coderule.minimq.domain.domain.cluster.route.RouteInfo;
import java.lang.Exception;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteLoader implements Lifecycle {
    private static final int DEFAULT_LOAD_INTERVAL = 30_000;
    private static final int DEFAULT_LOAD_TIMEOUT = 3_000;

    private final int loadInterval;
    private final int loadTimeout;

    private final RegistryClient registryClient;
    private final RouteCache route;

    private final ScheduledExecutorService scheduler;

    public RouteLoader(RegistryClient registryClient) {
        this(registryClient, DEFAULT_LOAD_INTERVAL);
    }

    public RouteLoader(RegistryClient registryClient, int loadInterval) {
        this(registryClient, loadInterval, DEFAULT_LOAD_TIMEOUT);
    }

    public RouteLoader(RegistryClient registryClient, int loadInterval, int loadTimeout) {
        this.registryClient = registryClient;
        this.loadInterval = loadInterval;
        this.loadTimeout = loadTimeout;

        this.route = new RouteCache();
        this.scheduler = ThreadUtil.newSingleScheduledThreadExecutor(
            new DefaultThreadFactory("BrokerRouteScheduler")
        );
    }

    @Override
    public void start() throws Exception {
        this.scheduler.scheduleAtFixedRate(
            RouteLoader.this::load,
            1000,
            loadInterval,
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void shutdown() throws Exception {
        this.scheduler.shutdown();
    }

    public void updateRouteInfo(String topicName, boolean updatePubInfo, boolean updateSubInfo) {
        if (!route.tryLock()) {
            return;
        }

        try {
            RouteInfo routeInfo = registryClient.syncRouteInfo(topicName, loadTimeout);
            if (routeInfo == null) {
                log.warn("Load route info error, topic: {}", topicName);
                return;
            }

            if (updatePubInfo) {
                route.updateRoute(topicName, routeInfo);
            }

            if (updateSubInfo) {
                route.updateSubscription(topicName, routeInfo);
            }
        } catch (Exception e) {
            handleRouteUpdateException(e, topicName);
        } finally {
            route.unlock();
        }
    }

    public RouteInfo getRoute(String topicName) {
        return route.getRoute(topicName);
    }

    public PublishInfo getPublishInfo(String topicName) {
        PublishInfo result = route.getPublishInfo(topicName);

        if (null == result || !result.isOk()) {
            updateRouteInfo(topicName, true, false);
            result = route.getPublishInfo(topicName);
        }

        return result;
    }

    public Set<MessageQueue> getSubscriptionInfo(String topicName) {
        Set<MessageQueue> result = route.getSubscription(topicName);

        if (CollectionUtil.isEmpty(result)) {
            updateRouteInfo(topicName, false, true);
            result = route.getSubscription(topicName);
        }

        return result;
    }

    public String getAddressInPublish(String groupName) {
        return route.getAddress(groupName, MQConstants.MASTER_ID);
    }

    public String getAddressInSubscription(String groupName, long groupNo, boolean inGroup) {
        String address = route.getAddress(groupName, groupNo);

        boolean isSlave = groupNo != MQConstants.MASTER_ID;
        if (StringUtil.isBlank(address) && isSlave) {
            address = route.getAddress(groupName, groupNo + 1);
        }

        if (StringUtil.isBlank(address) && inGroup) {
            address = route.getFirstAddress(groupName);
        }

        return address;
    }

    private void handleRouteUpdateException(Exception e, String topicName) {
        log.error("Load route info Exception", e);

        if (!(e instanceof RpcException rpcException)) {
            return;
        }

        if (NamespaceUtils.isRetryTopic(topicName)) {
            return;
        }

        if (ResponseCode.TOPIC_NOT_EXIST != rpcException.getCode()) {
            return;
        }

        route.removeSubscription(topicName);
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
