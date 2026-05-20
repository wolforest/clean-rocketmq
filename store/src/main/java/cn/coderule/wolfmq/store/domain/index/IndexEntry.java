package cn.coderule.wolfmq.store.domain.index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexEntry {
    public static final int INDEX_SIZE = 20;

    private int keyHash;
    private long phyOffset;
    private int timeDiff;
    private int slotValue;
}