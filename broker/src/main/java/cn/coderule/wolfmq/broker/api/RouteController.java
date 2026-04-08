package cn.coderule.wolfmq.broker.api;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.net.Address;
import cn.coderule.wolfmq.broker.api.validator.GroupValidator;
import cn.coderule.wolfmq.broker.domain.meta.RouteService;
import cn.coderule.wolfmq.broker.domain.meta.SubscriptionService;
import cn.coderule.wolfmq.broker.domain.meta.BrokerTopicService;
import cn.coderule.wolfmq.domain.core.enums.message.MessageType;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.meta.topic.TopicValidator;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RouteController {
    private final RouteService routeService;
    private final BrokerTopicService topicService;
    private final SubscriptionService subscriptionService;

    public RouteController(RouteService routeService, BrokerTopicService topicService, SubscriptionService subscriptionService) {
        this.routeService = routeService;
        this.topicService = topicService;
        this.subscriptionService = subscriptionService;
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

        if (StringUtil.notBlank(context.getConsumeGroup())) {
            GroupValidator.validate(context.getConsumeGroup());
        }

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
