package cn.coderule.minimq.domain.domain.consumer.consume.mq;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueResult implements Serializable {
    private long minOffset;
    private long maxOffset;

    public static QueueResult minOffset(long minOffset) {
        return QueueResult.builder()
            .minOffset(minOffset)
            .build();
    }

    public static QueueResult maxOffset(long maxOffset) {
        return QueueResult.builder()
            .maxOffset(maxOffset)
            .build();
    }
}

