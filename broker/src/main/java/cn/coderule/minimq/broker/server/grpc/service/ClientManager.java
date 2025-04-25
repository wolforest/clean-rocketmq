package cn.coderule.minimq.broker.server.grpc.service;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.Settings;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.ArrayUtil;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientManager extends ServiceThread implements Lifecycle {
    private static final int WAIT_INTERVAL = 5_000;
    // clientId -> settings
    private static final ConcurrentMap<String, Settings> SETTING_MAP = new ConcurrentHashMap<>();

    @Override
    public String getServiceName() {
        return ClientManager.class.getSimpleName();
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
