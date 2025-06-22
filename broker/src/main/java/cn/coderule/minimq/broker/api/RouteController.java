package cn.coderule.minimq.broker.api;

import cn.coderule.common.util.net.Address;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.broker.domain.meta.SubscriptionService;
import cn.coderule.minimq.broker.domain.meta.TopicService;
import cn.coderule.minimq.domain.config.message.TopicConfig;
import cn.coderule.minimq.domain.domain.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.model.meta.topic.TopicValidator;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.cluster.route.RouteInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RouteController {
    private final RouteService routeService;
    private final TopicConfig topicConfig;

    private TopicService topicService;
    private SubscriptionService subscriptionService;

    public RouteController(TopicConfig topicConfig, RouteService routeService) {
        this.routeService = routeService;
        this.topicConfig = topicConfig;
    }

    public Topic getTopic(String topicName) {
        TopicValidator.validateTopic(topicName);
        return topicService.getTopic(topicName);
    }

    public CompletableFuture<Topic> getTopicAsync(String topicName) {
        TopicValidator.validateTopic(topicName);
        return topicService.getTopicAsync(topicName);
    }

    public MessageType getTopicType(String topicName) {
        TopicValidator.validateTopic(topicName);
        return topicService.getTopicType(topicName);
    }

    public SubscriptionGroup getGroup(String topicName, String groupName) {
        return subscriptionService.getGroup(topicName, groupName);
    }

    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        return subscriptionService.getGroupAsync(topicName, groupName);
    }

    public boolean isConsumeOrderly(String topicName, String groupName) {
        return subscriptionService.isConsumeOrderly(topicName, groupName);
    }

    public CompletableFuture<RouteInfo> getRoute(RequestContext context, String topicName, List<Address> addressList) {
        TopicValidator.validateTopic(topicName);

        RouteInfo routeInfo = routeService.get(context, topicName, addressList);
        return CompletableFuture.completedFuture(routeInfo);
    }

    public CompletableFuture<RouteInfo> getRoute(RequestContext context, String topicName, String groupName,
        List<Address> addressList) throws ExecutionException, InterruptedException {
        RouteInfo routeInfo = getRoute(context, topicName, addressList).get();

        boolean isConsumeOrderly = subscriptionService.isConsumeOrderly(topicName, groupName);
        routeInfo.setConsumeOrderly(isConsumeOrderly);

        return CompletableFuture.completedFuture(routeInfo);
    }

}
