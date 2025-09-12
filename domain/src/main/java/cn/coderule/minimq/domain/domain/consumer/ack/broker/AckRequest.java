package cn.coderule.minimq.domain.domain.consumer.ack.broker;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
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
