package cn.coderule.minimq.domain.domain.meta.order;

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
public class OrderRequest implements Serializable {
    private RequestContext requestContext;
    private long timeout;

    private String topicName;
    private String consumerGroup;
    private int queueId;
    private boolean isRetry;

    private long popTime;
    private long invisibleTime;

}
