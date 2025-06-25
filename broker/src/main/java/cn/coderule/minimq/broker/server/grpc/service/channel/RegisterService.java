package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.FilterExpression;
import apache.rocketmq.v2.Resource;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.SubscriptionEntry;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.minimq.broker.server.grpc.converter.GrpcConverter;
import cn.coderule.minimq.domain.core.constant.MQVersion;
import cn.coderule.minimq.domain.domain.model.consumer.ConsumerInfo;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.enums.code.LanguageCode;
import cn.coderule.minimq.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.minimq.domain.core.enums.consume.ConsumeType;
import cn.coderule.minimq.domain.core.enums.message.MessageModel;
import cn.coderule.minimq.domain.core.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.rpc.broker.core.FilterAPI;
import cn.coderule.minimq.rpc.common.grpc.core.exception.GrpcException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class RegisterService {
    private final ChannelManager channelManager;

    private ProducerController producerController;
    private ConsumerController consumerController;
    private TransactionController transactionController;
    private RouteController routeController;

    public RegisterService(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public void inject(
        RouteController routeController,
        ProducerController producerController,
        ConsumerController consumerController,
        TransactionController transactionController
    ) {
        this.routeController = routeController;
        this.producerController = producerController;
        this.consumerController = consumerController;
        this.transactionController = transactionController;
    }

    public GrpcChannel registerConsumer(RequestContext context, String consumerGroup, ClientType clientType, Settings settings, boolean updateSubscription) {
        ClientChannelInfo channelInfo = createChannelInfo(context);

        Set<SubscriptionData> subscriptionDataSet = buildSubscriptionDataSet(
            settings.getSubscription().getSubscriptionsList()
        );

        ConsumerInfo consumerInfo = ConsumerInfo.builder()
            .groupName(consumerGroup)
            .messageModel(MessageModel.CLUSTERING)
            .consumeType(buildConsumeType(clientType))
            .consumeStrategy(ConsumeStrategy.CONSUME_FROM_LAST_OFFSET)
            .channelInfo(channelInfo)
            .subscriptionSet(subscriptionDataSet)
            .enableNotification(false)
            .enableSubscriptionModification(updateSubscription)
            .build();

        consumerController.register(context, consumerInfo);

        return (GrpcChannel) channelInfo.getChannel();
    }

    public void registerProducer(RequestContext context, Settings settings) {
        for (Resource topic : settings.getPublishing().getTopicsList()) {
            String topicName = topic.getName();
            registerProducer(context, topicName);
        }
    }

    public GrpcChannel registerProducer(RequestContext context, String topicName) {
        ClientChannelInfo channelInfo = createChannelInfo(context);

        producerController.register(context, topicName, channelInfo);
        transactionSubscribe(context, topicName);

        return (GrpcChannel) channelInfo.getChannel();
    }

    private void transactionSubscribe(RequestContext context, String topicName) {
        MessageType messageType = routeController.getTopicType(topicName);
        if (!messageType.isTransaction()) {
            return;
        }

        transactionController.subscribe(context, topicName, topicName);
    }

    private ClientChannelInfo createChannelInfo(RequestContext context) {
        String clientId = context.getClientID();
        LanguageCode languageCode = LanguageCode.valueOf(context.getLanguage());
        GrpcChannel channel = channelManager.createChannel(context, clientId);
        int version = parseClientVersion(context.getClientVersion());

        return ClientChannelInfo.builder()
            .clientId(clientId)
            .channel(channel)
            .language(languageCode)
            .version(version)
            .lastUpdateTime(System.currentTimeMillis())
            .build();
    }

    private ConsumeType buildConsumeType(ClientType clientType) {
        return switch (clientType) {
            case SIMPLE_CONSUMER -> ConsumeType.CONSUME_ACTIVELY;
            case PUSH_CONSUMER -> ConsumeType.CONSUME_PASSIVELY;
            default -> throw new IllegalArgumentException(
                "Client type is not consumer, type: " + clientType);
        };
    }

    private Set<SubscriptionData> buildSubscriptionDataSet(List<SubscriptionEntry> subscriptionEntryList) {
        Set<SubscriptionData> subscriptionDataSet = new HashSet<>();
        for (SubscriptionEntry sub : subscriptionEntryList) {
            String topicName = sub.getTopic().getName();
            FilterExpression filterExpression = sub.getExpression();
            subscriptionDataSet.add(buildSubscriptionData(topicName, filterExpression));
        }
        return subscriptionDataSet;
    }

    private SubscriptionData buildSubscriptionData(String topicName, FilterExpression filterExpression) {
        String expression = filterExpression.getExpression();
        String expressionType = GrpcConverter.getInstance().buildExpressionType(filterExpression.getType());
        try {
            return FilterAPI.build(topicName, expression, expressionType);
        } catch (Exception e) {
            throw new GrpcException(InvalidCode.ILLEGAL_FILTER_EXPRESSION, "expression format is not correct", e);
        }
    }

    private int parseClientVersion(String clientVersionStr) {
        int clientVersion = MQVersion.CURRENT_VERSION;
        if (StringUtils.isEmpty(clientVersionStr)) {
            return clientVersion;
        }

        try {
            String tmp = StringUtils.upperCase(clientVersionStr);
            clientVersion = MQVersion.Version.valueOf(tmp).ordinal();
        } catch (Exception ignored) {
        }

        return clientVersion;
    }
}
