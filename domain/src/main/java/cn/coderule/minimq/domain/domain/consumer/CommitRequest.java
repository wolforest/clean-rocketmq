package cn.coderule.minimq.domain.domain.model.consumer;

import cn.coderule.minimq.domain.domain.core.enums.produce.TransactionStatus;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class CommitRequest implements Serializable {
    private RequestContext requestContext;
    private String transactionId;
    private String messageId;

    private String topicName;
    private String producerGroup;

    private long timeout;
    private boolean byCheck;
    private TransactionStatus status;
}
