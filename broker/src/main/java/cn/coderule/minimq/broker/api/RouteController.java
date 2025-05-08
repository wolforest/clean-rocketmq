package cn.coderule.minimq.broker.api;

import cn.coderule.common.util.net.Address;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.domain.config.TopicConfig;
import cn.coderule.minimq.domain.domain.model.meta.topic.TopicValidator;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.domain.domain.model.cluster.route.RouteInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RouteController {
    private final RouteService routeService;
    private final TopicConfig topicConfig;

    public RouteController(TopicConfig topicConfig, RouteService routeService) {
        this.routeService = routeService;
        this.topicConfig = topicConfig;
    }

    public CompletableFuture<RouteInfo> getRoute(RequestContext context, String topicName, List<Address> addressList) {
        TopicValidator.validateTopic(topicName);

        RouteInfo routeInfo = routeService.get(context, topicName, addressList);
        return CompletableFuture.completedFuture(routeInfo);
    }

    public CompletableFuture<RouteInfo> getRoute(RequestContext context, String topicName, String groupName,
        List<Address> addressList) throws ExecutionException, InterruptedException {
        RouteInfo routeInfo = getRoute(context, topicName, addressList).get();

        boolean isConsumeOrderly = routeService.isConsumeOrderly(topicName, groupName);
        routeInfo.setConsumeOrderly(isConsumeOrderly);

        return CompletableFuture.completedFuture(routeInfo);
    }

}
