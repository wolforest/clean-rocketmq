package cn.coderule.minimq.domain.domain.consumer.consume.mq;

import cn.coderule.minimq.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.minimq.domain.service.store.domain.mq.MessageFilter;
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
    private String storeGroup;

    private String group;
    private String topic;
    private int queueId;
    private int reviveQueueId;
    private long offset;

    private long dequeueTime;
    private long invisibleTime;

    @Builder.Default
    private int num = 1;
    private int maxNum;
    @Builder.Default
    private boolean fifo = false;
    @Builder.Default
    private boolean checkResetOffset = false;

    @Builder.Default
    private MessageFilter filter = null;
    private ConsumeStrategy consumeStrategy;
}

