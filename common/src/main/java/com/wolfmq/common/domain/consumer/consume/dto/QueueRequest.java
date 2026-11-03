package com.wolfmq.common.domain.consumer.consume.dto;

import com.wolfmq.common.server.RequestContext;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueRequest implements Serializable {
    RequestContext requestContext;
    private String storeGroup;
    private String consumerGroup;
    private String topicName;
    private int queueId;
}

