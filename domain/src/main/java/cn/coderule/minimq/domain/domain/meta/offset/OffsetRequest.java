package cn.coderule.minimq.domain.domain.meta.offset;

import cn.coderule.minimq.domain.domain.MessageQueue;
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
    private long timeout;

    private MessageQueue messageQueue;
    private String consumerGroup;

    private long newOffset;
}
