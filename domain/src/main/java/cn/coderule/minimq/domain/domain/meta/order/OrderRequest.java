package cn.coderule.minimq.domain.domain.meta.order;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.io.Serializable;
import java.util.List;
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
    private String storeGroup;

    String attemptId;
    @Builder.Default
    private boolean isRetry = false;

    private String topicName;
    private String consumerGroup;
    private int queueId;

    private long dequeueTime;
    private long invisibleTime;

    private long queueOffset;
    private List<Long> offsetList;
    @Builder.Default
    StringBuilder orderInfoBuilder = new StringBuilder(64);

    public String getKey() {
        return OrderUtils.buildKey(topicName, consumerGroup);
    }

}
