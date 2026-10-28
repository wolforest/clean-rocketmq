package com.wolfmq.domain.domain.consumer.ack.store;

import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import java.io.Serializable;
import lombok.Data;

@Data
public class CheckPointRequest implements Serializable {
    private RequestContext requestContext;

    private String storeGroup;

    private PopCheckPoint checkPoint;
    private int reviveQueueId;
    private long reviveQueueOffset;
    private long nextOffset;
}
