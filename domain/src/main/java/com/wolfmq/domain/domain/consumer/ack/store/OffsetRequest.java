package com.wolfmq.domain.domain.consumer.ack.store;

import com.wolfmq.domain.server.RequestContext;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffsetRequest implements Serializable {
    private RequestContext requestContext;

    private String storeGroup;

    private String topicName;
    private String groupName;
    private int queueId;
}
