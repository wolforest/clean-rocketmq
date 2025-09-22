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
    @Builder.Default
    private long offset = 0;
    @Builder.Default
    private long minOffset = 0;
    @Builder.Default
    private long maxOffset = 0;

    public static QueueResult offset(long offset) {
        return QueueResult.builder()
            .offset(offset)
            .build();
    }

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

