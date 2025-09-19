package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.core.enums.TransactionStatus;
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
public class SubmitRequest implements Serializable {
    private RequestContext requestContext;
    private String transactionId;
    private String messageId;

    private String topicName;
    private String producerGroup;

    private long timeout;
    private boolean byCheck;
    private TransactionStatus status;
}
