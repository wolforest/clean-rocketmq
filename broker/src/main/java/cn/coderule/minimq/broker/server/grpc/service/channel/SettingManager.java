package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.CustomizedBackoff;
import apache.rocketmq.v2.ExponentialBackoff;
import apache.rocketmq.v2.Settings;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.ArrayUtil;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.domain.config.GrpcConfig;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.CustomizedRetryPolicy;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.ExponentialRetryPolicy;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.GroupRetryPolicy;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.GroupRetryPolicyType;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.SubscriptionGroup;
import com.google.protobuf.Duration;
import com.google.protobuf.util.Durations;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SettingManager extends ServiceThread implements Lifecycle {
    private static final int WAIT_INTERVAL = 5_000;
    // clientId -> settings
    private static final ConcurrentMap<String, Settings> SETTING_MAP = new ConcurrentHashMap<>();

    private final GrpcConfig config;
    private ConsumerController consumerController;

    public SettingManager(GrpcConfig config) {
        this.config = config;
    }

    public void inject(ConsumerController consumerController) {
        this.consumerController = consumerController;
    }

    @Override
    public String getServiceName() {
        return SettingManager.class.getSimpleName();
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            try {
                this.removeExpiredClient();
                this.await(WAIT_INTERVAL);
            } catch (Exception e) {
                log.error("{} service has exception. ", this.getServiceName(), e);
            }
        }
    }

    private void removeExpiredClient() {
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

    public void updateSettings(String clientId, Settings settings) {
        if (settings.hasSubscription()) {
            settings = createSettingsBuilder()
                .mergeFrom(settings)
                .build();
        }

        SETTING_MAP.put(clientId, settings);
    }

    public Settings removeSettings(String clientId) {
        return SETTING_MAP.remove(clientId);
    }

    public Settings removeSettings(RequestContext context) {
        String clientId = context.getClientID();
        Settings settings = removeSettings(clientId);

        if (settings == null) {
            return null;
        }

        if (settings.hasSubscription()) {
            settings = mergeSubscriptionSettings(settings, context);
        }

        return mergeMetric(settings);
    }

    private Settings mergeProducerSettings(Settings settings) {
        Settings.Builder builder = Settings.newBuilder();

        ExponentialBackoff backoff = ExponentialBackoff.newBuilder()
            .setInitial(Durations.fromMillis(config.getProducerBackoffMillis()))
            .setMax(Durations.fromMillis(config.getProducerMaxBackoffMillis()))
            .setMultiplier(config.getProducerBackoffMultiplier())
            .build();

        builder.getBackoffPolicyBuilder()
            .setMaxAttempts(config.getProducerMaxAttempts())
            .setExponentialBackoff(backoff);

        builder.getPublishingBuilder()
            .setValidateMessageType(config.isEnableMessageTypeCheck())
            .setMaxBodySize(config.getMaxMessageSize());

        return builder.build();
    }

    private Settings mergeSubscriptionSettings(Settings settings, RequestContext context) {
        SubscriptionGroup subscription = getSubscription(settings, context);
        if (subscription == null) {
            return settings;
        }

        return mergeSubscription(settings, subscription);
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

            String topic = settings.getSubscription().getSubscriptions(0).getTopic().getName();
            String consumerGroup = settings.getSubscription().getGroup().getName();

            return settings;
        });
    }

    private SubscriptionGroup getSubscription(Settings settings, RequestContext context) {
        String group = settings.getSubscription().getGroup().getName();
        String topic = settings.getSubscription().getSubscriptions(0).getTopic().getName();

        try {
            return consumerController.getSubscription(context, topic, group).get();
        } catch (Throwable e) {
            log.error("get subscription failed. clientId:{}, topic:{}, group:{}.",
                context.getClientID(), topic, group, e);
        }

        return null;
    }

    protected Settings mergeSubscription(Settings settings, SubscriptionGroup groupConfig) {
        Settings.Builder builder = settings.toBuilder();

        builder.getSubscriptionBuilder()
            .setReceiveBatchSize(config.getConsumerPollBatchSize())
            .setLongPollingTimeout(Durations.fromMillis(config.getConsumerMaxPollTime()))
            .setFifo(groupConfig.isConsumeMessageOrderly());

        builder.getBackoffPolicyBuilder()
            .setMaxAttempts(groupConfig.getRetryMaxTimes() + 1);

        GroupRetryPolicy retryPolicy = groupConfig.getGroupRetryPolicy();
        if (retryPolicy.getType().equals(GroupRetryPolicyType.EXPONENTIAL)) {
            ExponentialRetryPolicy exponentialRetryPolicy = retryPolicy.getExponentialRetryPolicy();
            if (exponentialRetryPolicy == null) {
                exponentialRetryPolicy = new ExponentialRetryPolicy();
            }
            builder.getBackoffPolicyBuilder().setExponentialBackoff(convertToExponentialBackoff(exponentialRetryPolicy));
        } else {
            CustomizedRetryPolicy customizedRetryPolicy = retryPolicy.getCustomizedRetryPolicy();
            if (customizedRetryPolicy == null) {
                customizedRetryPolicy = new CustomizedRetryPolicy();
            }
            builder.getBackoffPolicyBuilder()
                .setCustomizedBackoff(convertToCustomizedRetryPolicy(customizedRetryPolicy));
        }

        return builder.build();
    }

    private ExponentialBackoff convertToExponentialBackoff(ExponentialRetryPolicy retryPolicy) {
        return ExponentialBackoff.newBuilder()
            .setInitial(Durations.fromMillis(retryPolicy.getInitial()))
            .setMax(Durations.fromMillis(retryPolicy.getMax()))
            .setMultiplier(retryPolicy.getMultiplier())
            .build();
    }

    private CustomizedBackoff convertToCustomizedRetryPolicy(CustomizedRetryPolicy retryPolicy) {
        List<Duration> durationList = Arrays.stream(retryPolicy.getNext())
            .mapToObj(Durations::fromMillis).collect(Collectors.toList());

        return CustomizedBackoff.newBuilder()
            .addAllNext(durationList)
            .build();
    }

    private Settings.Builder createSettingsBuilder() {
        Settings settings = Settings.newBuilder()
            .getDefaultInstanceForType();

        settings = mergeSubscription(settings, new SubscriptionGroup());

        return settings.toBuilder();
    }
}
