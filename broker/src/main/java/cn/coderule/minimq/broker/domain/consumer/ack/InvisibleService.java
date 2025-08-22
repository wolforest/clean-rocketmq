package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import java.util.concurrent.CompletableFuture;

public class InvisibleService {
    public CompletableFuture<AckResult> changeInvisible(InvisibleRequest request) {
        return null;
    }
}
