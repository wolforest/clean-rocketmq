package cn.coderule.minimq.domain.domain.dto.request;

import cn.coderule.minimq.domain.domain.enums.produce.TransactionStatus;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.service.broker.consume.PopFilter;
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
