package com.wolfmq.common.domain.consumer.ack.broker;

import com.wolfmq.common.server.broker.RequestContext;
import java.io.Serializable;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckRequest implements Serializable {
    private RequestContext requestContext;

    private String messageId;
    private String receiptStr;
    private String topicName;
    private String groupName;

    @Builder.Default
    private long timeout = Duration.ofSeconds(2).toMillis();


}
