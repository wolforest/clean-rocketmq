package cn.coderule.minimq.broker.api;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.net.Address;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.domain.config.TopicConfig;
import cn.coderule.minimq.domain.domain.enums.InvalidCode;
import cn.coderule.minimq.domain.domain.exception.InvalidParameterException;
import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.domain.domain.model.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.utils.topic.TopicValidator;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RouteController {
    private final RouteService routeService;
    private final TopicConfig topicConfig;

    public RouteController(TopicConfig topicConfig, RouteService routeService) {
        this.routeService = routeService;
        this.topicConfig = topicConfig;
    }

    public CompletableFuture<RouteInfo> getRoute(RequestContext context, String topicName, List<Address> addressList) {
        validateTopic(topicName);

        RouteInfo routeInfo = routeService.get(context, topicName, addressList);
        return CompletableFuture.completedFuture(routeInfo);
    }

    public CompletableFuture<Topic> getTopic(String topicName) {
        return null;
    }

    public CompletableFuture<SubscriptionGroup> getSubscription(String topicName, String groupName) {
        return null;
    }

    private void validateTopic(String topicName) {
        if (StringUtil.isBlank(topicName)) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is blank");
        }

        if (topicName.length() > topicConfig.getMaxTopicLength()) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is too long");
        }

        if (TopicValidator.isTopicOrGroupIllegal(topicName)) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is illegal");
        }

        if (TopicValidator.isSystemTopic(topicName)) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "topicName is system topic");
        }
    }

}
