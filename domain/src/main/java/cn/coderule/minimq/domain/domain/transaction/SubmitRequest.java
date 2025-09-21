package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.core.enums.TransactionType;
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
public class SubmitRequest implements Serializable {
    private RequestContext requestContext;
    private String transactionId;
    private String messageId;

    private String topicName;
    private String producerGroup;

    private boolean fromCheck;
    private int transactionFlag;
    private TransactionType transactionType;

    private long queueOffset;
    private long commitOffset;

    @Builder.Default
    private long timeout = Duration.ofSeconds(2).toMillis();
}
