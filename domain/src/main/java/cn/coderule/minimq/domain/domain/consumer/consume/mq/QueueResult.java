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
}

