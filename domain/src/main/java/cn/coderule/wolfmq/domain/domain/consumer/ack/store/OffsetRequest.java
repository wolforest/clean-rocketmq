package cn.coderule.wolfmq.domain.domain.consumer.ack.store;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
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
