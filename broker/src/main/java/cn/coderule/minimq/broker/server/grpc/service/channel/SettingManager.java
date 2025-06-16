package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.Settings;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.ArrayUtil;
import cn.coderule.minimq.domain.config.GrpcConfig;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SettingManager extends ServiceThread implements Lifecycle {
    private static final int WAIT_INTERVAL = 5_000;
    // clientId -> settings
    private static final ConcurrentMap<String, Settings> SETTING_MAP = new ConcurrentHashMap<>();

    private final GrpcConfig config;

    public SettingManager(GrpcConfig config) {
        this.config = config;
    }

    @Override
    public String getServiceName() {
        return SettingManager.class.getSimpleName();
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            try {
                this.await(WAIT_INTERVAL);
            } catch (Exception e) {
                log.error("{} service has exception. ", this.getServiceName(), e);
            }
        }
    }

    @Override
    protected void postAwait() {
        Set<String> clientIdSet = SETTING_MAP.keySet();
        for (String clientId : clientIdSet) {
            try {
                removeExpiredClient(clientId);
            } catch (Throwable e) {
                log.error("remove expired grpc client settings failed. clientId:{}", clientId, e);
            }
        }
    }

    public Settings getSettings(String clientId) {
        return SETTING_MAP.get(clientId);
    }

    public Settings getSettings(RequestContext context) {
        String clientId = context.getClientID();
        Settings settings = getSettings(clientId);
        if (settings == null) {
            log.warn("clientId:{} not found settings.", clientId);
            return null;
        }

        if (settings.hasPublishing()) {
            settings = mergeProducerSettings(settings);
        }

        if (settings.hasSubscription()) {
            settings = mergeSubscriptionSettings(settings, context);
        }

        return mergeMetric(settings);
    }

    private Settings mergeProducerSettings(Settings settings) {
        return settings;
    }

    private Settings mergeSubscriptionSettings(Settings settings, RequestContext context) {
        String group = settings.getSubscription().getGroup().getName();

        settings.getSubscription().getSubscriptions(0).getTopic().getName();

        return settings;
    }

    private Settings mergeMetric(Settings settings) {
        return settings;
    }



    private void removeExpiredClient(String clientId) {
        SETTING_MAP.computeIfPresent(clientId, (clientKey, settings) -> {
            if (!ArrayUtil.inArray(settings.getClientType(),
                ClientType.PUSH_CONSUMER, ClientType.SIMPLE_CONSUMER)) {
                return settings;
            }

            String consumerGroup = settings.getSubscription().getGroup().getName();

            return settings;
        });
    }
}
