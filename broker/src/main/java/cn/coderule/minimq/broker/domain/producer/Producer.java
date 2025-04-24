package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Producer implements Lifecycle {
    private ThreadPoolExecutor executor;

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        // validate topic
        // validate message
        // select message queue
        // init uuid

        // send message
        // execute send callback
        // execute complete callback

        return null;
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        if (CollectionUtil.isEmpty(messageList)) {
            return CompletableFuture.completedFuture(List.of());
        }

        List<CompletableFuture<EnqueueResult>> futureList = new ArrayList<>();
        for (MessageBO messageBO : messageList) {
            CompletableFuture<EnqueueResult> future = produce(context, messageBO);
            futureList.add(future);
        }

        return combineEnqueueResult(futureList);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    private CompletableFuture<List<EnqueueResult>> combineEnqueueResult(List<CompletableFuture<EnqueueResult>> futureList) {
        CompletableFuture<Void> all = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        return all.thenApply(v -> {
            List<EnqueueResult> resultList = new ArrayList<>();
            for (CompletableFuture<EnqueueResult> future : futureList) {
                addEnqueueResult(resultList, future);
            }
            return resultList;
        });
    }

    private void addEnqueueResult(List<EnqueueResult> resultList, CompletableFuture<EnqueueResult> future) {
        try {
            EnqueueResult enqueueResult = future.get();
            resultList.add(enqueueResult);
        } catch (Throwable t) {
            log.error("produce message error", t);
            resultList.add(EnqueueResult.failure());
        }
    }

}
