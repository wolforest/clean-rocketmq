package cn.coderule.minimq.hello.java.concurrent.future;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {
    private static final int QUEUE_NUM = 16;
    private static final AtomicLong counter = new AtomicLong(0);

    public static void main(String[] args) {
        PopService popService = new PopService();
        popService.pop();
    }

    public void pop() {
        PopContext context = new PopContext();
        CompletableFuture<PopResult> result = PopResult.future();

        for (int i = 0; i < QUEUE_NUM; i++) {
            int finalI = i;
            result = result.thenCompose(
                res -> popFromQueue(context, finalI, res)
            );
        }
    }

    private CompletableFuture<PopResult> popFromQueue(PopContext context, int queueId, PopResult lastResult) {
        return popFromStore(context, queueId)
            .thenApply(
                result -> processStoreResult(context, queueId, result, lastResult)
            );
    }

    private CompletableFuture<PopResult> popFromStore(PopContext context, int queueId) {
        PopResult result = new PopResult();
        result.setRestNum(queueId);
        return CompletableFuture.supplyAsync(() -> result);
    }

    private PopResult processStoreResult(PopContext context, int queueId, PopResult storeResult, PopResult lastResult) {
        PopResult newResult = new PopResult();

        ArrayList<Object> messageList = new ArrayList<>();
        messageList.addAll(lastResult.getMessageList());
        messageList.addAll(storeResult.getMessageList());
        newResult.setMessageList(messageList);

        newResult.setRestNum(
            lastResult.getRestNum() + storeResult.getMessageList().size()
        );

        return newResult;
    }

    static class PopContext {
    }

    @Data
    static class PopResult implements Serializable {
        private List<Object> messageList = new ArrayList<>();
        private long restNum = 0;

        public static CompletableFuture<PopResult> future() {
            return CompletableFuture.completedFuture(new PopResult());
        }
    }
}
