package cn.coderule.wolfmq.store.domain.index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexHeader {
    public static final int HEADER_SIZE = 40;

    private long beginTimestamp;
    private long endTimestamp;
    private long beginPhyOffset;
    private long endPhyOffset;
    private int hashSlotCount;
    private int indexCount;

    public void updateTimeDiff(long timestamp) {
        if (this.beginTimestamp == 0) {
            this.beginTimestamp = timestamp;
        }
        this.endTimestamp = timestamp;
    }

    public void updatePhyOffset(long phyOffset) {
        if (this.beginPhyOffset == 0) {
            this.beginPhyOffset = phyOffset;
        }
        this.endPhyOffset = phyOffset;
    }

    public void incrementIndexCount() {
        this.indexCount++;
    }
}