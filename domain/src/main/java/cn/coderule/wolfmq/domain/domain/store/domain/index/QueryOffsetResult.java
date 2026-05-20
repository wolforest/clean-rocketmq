package cn.coderule.wolfmq.domain.domain.store.domain.index;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueryOffsetResult {
    private List<Long> phyOffsets;
    private long indexLastUpdateTimestamp;
    private long indexLastUpdatePhyOffset;
}