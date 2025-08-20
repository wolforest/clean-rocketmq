package cn.coderule.minimq.domain.domain.meta.order;

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
    String attemptId;
    private boolean isRetry;

    private String topicName;
    private String consumerGroup;
    private int queueId;

    private long popTime;
    private long invisibleTime;

    private List<Long> offsetList;
    StringBuilder orderInfoBuilder;

    private String getKey() {
        return KeyUtils.buildKey(topicName, consumerGroup);
    }

}
