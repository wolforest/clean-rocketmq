package cn.coderule.minimq.registry.processor;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.kv.KVService;
import cn.coderule.minimq.registry.domain.store.service.TopicService;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteProcessor implements RpcProcessor {
    private final RegistryConfig registryConfig;

    private final TopicService topicService;
    private final KVService kvService;

    private final long startTime;
    private final AtomicBoolean isServerReady;

    public RouteProcessor(RegistryConfig registryConfig, TopicService topicService, KVService kvService) {
        this.registryConfig = registryConfig;

        this.topicService = topicService;
        this.kvService = kvService;

        this.startTime = System.currentTimeMillis();
        isServerReady = new AtomicBoolean(false);
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) {
        return null;
    }

    @Override
    public boolean reject() {
        return false;
    }
}
