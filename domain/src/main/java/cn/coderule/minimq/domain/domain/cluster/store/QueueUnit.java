package cn.coderule.minimq.domain.domain.cluster.store;

import java.io.Serializable;
import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueueUnit implements Serializable {
    private long queueOffset;

    private long commitLogOffset;
    private int messageSize;
    @Builder.Default
    private short batchNum = 1;

    private long tagsCode;
    private ByteBuffer buffer;

    private int unitSize;

    public boolean isValid() {
        return commitLogOffset != 0
            && messageSize != Integer.MAX_VALUE
            && tagsCode != 0;
    }
}
