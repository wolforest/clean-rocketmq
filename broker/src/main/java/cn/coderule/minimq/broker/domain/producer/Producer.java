package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.model.RequestContext;
import cn.coderule.minimq.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.dto.EnqueueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

public class Producer implements Lifecycle {
    private ThreadPoolExecutor executor;

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        return null;
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

}
