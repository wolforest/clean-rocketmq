package cn.coderule.minimq.registry.processor;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.kv.KVService;
import cn.coderule.minimq.registry.domain.store.service.TopicService;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.SystemResponseCode;
import cn.coderule.minimq.rpc.registry.protocol.header.GetRouteInfoRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteProcessor implements RpcProcessor {
    private final RegistryConfig registryConfig;

    private final TopicService topicService;
    private final KVService kvService;

    private final long startTime;
    private final AtomicBoolean isServerBooting;

    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.GET_ROUTEINFO_BY_TOPIC
    );


    public RouteProcessor(
        RegistryConfig registryConfig,
        TopicService topicService,
        KVService kvService,
        ExecutorService executor) {
        this.registryConfig = registryConfig;

        this.topicService = topicService;
        this.kvService = kvService;
        this.executor = executor;

        this.startTime = System.currentTimeMillis();
        isServerBooting = new AtomicBoolean(true);
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        GetRouteInfoRequestHeader requestHeader = request.decodeHeader(GetRouteInfoRequestHeader.class);

        if (isServerNotReady(request, response)) {
            return response;
        }

        RouteInfo routeInfo = topicService.getRoute(requestHeader.getTopic());
        if (routeInfo == null) {
            return topicNotExist(requestHeader, response);
        }

        flushIsServerReady();
        setOrderConfig(requestHeader, routeInfo);
        response.setBody(getRouteBody(requestHeader, routeInfo));

        return response.success();
    }

    @Override
    public boolean reject() {
        return false;
    }

    private boolean isServerNotReady(RpcCommand request, RpcCommand response) {
        boolean isOverDelay = System.currentTimeMillis() - startTime > registryConfig.getServerStartupDelay();
        boolean serverNotReady = !(isServerBooting.get() && isOverDelay) && registryConfig.isWaitServerStartup();
        if (serverNotReady) {
            log.warn("registry server is not ready. request code: {}", request.getCode());
            response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "server not ready");
            return true;
        }

        return false;
    }

    private RpcCommand topicNotExist(GetRouteInfoRequestHeader requestHeader, RpcCommand response) {
        String remark = "topic not exist: " + requestHeader.getTopic();
        return response.setCodeAndRemark(ResponseCode.TOPIC_NOT_EXIST, remark);
    }

    private void setOrderConfig(GetRouteInfoRequestHeader requestHeader, RouteInfo routeInfo) {
        if (!registryConfig.isEnableOrderTopic()) {
            return;
        }

        String orderTopicConfig = kvService.getKVConfig(
            KVService.NAMESPACE_ORDER_TOPIC_CONFIG,
            requestHeader.getTopic()
        );
        routeInfo.setOrderTopicConf(orderTopicConfig);
    }

    private byte[] getRouteBody(GetRouteInfoRequestHeader requestHeader, RouteInfo routeInfo) {
        Boolean standardJson = Optional
            .ofNullable(requestHeader.getAcceptStandardJsonOnly())
            .orElse(false);

        return routeInfo.encode(!standardJson);
    }

    private void flushIsServerReady() {
        if (!isServerBooting.get()) {
            return;
        }

        isServerBooting.set(false);
    }

}
