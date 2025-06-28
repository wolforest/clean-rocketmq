package cn.coderule.minimq.domain.domain.consumer.consume;

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
public class QueueRequest implements Serializable {
    RequestContext context;

    private String group;
    private String topic;
    private int queueId;
}

