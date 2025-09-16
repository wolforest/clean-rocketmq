package cn.coderule.minimq.domain.domain.store.domain.mq;

import cn.coderule.minimq.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DequeueRequest implements Serializable {
    private RequestContext requestContext;
    private String storeGroup;
    private String attemptId;

    private String group;
    private String topic;
    private int queueId;
    private int reviveQueueId;
    private long offset;

    private long dequeueTime;
    private long invisibleTime;

    @Builder.Default
    private int maxNum = 1;
    @Builder.Default
    private boolean fifo = false;
    @Builder.Default
    private boolean checkResetOffset = false;
    @Builder.Default
    private boolean commitInitOffset = true;

    @Builder.Default
    private MessageFilter filter = null;
    private ConsumeStrategy consumeStrategy;

    public boolean isConsumeFromStart() {
        return consumeStrategy == ConsumeStrategy.CONSUME_FROM_START;
    }
}

