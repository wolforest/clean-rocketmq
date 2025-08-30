package cn.coderule.minimq.domain.domain.consumer.ack.store;

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
public class OffsetRequest implements Serializable {
    private RequestContext requestContext;

    private String storeGroup;

    private String topicName;
    private String groupName;
    private int queueId;
}
