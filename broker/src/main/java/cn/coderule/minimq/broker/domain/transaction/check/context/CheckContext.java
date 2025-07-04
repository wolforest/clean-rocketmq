package cn.coderule.minimq.broker.domain.transaction.check.context;

import cn.coderule.minimq.domain.config.TransactionConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckContext implements Serializable {
    private TransactionContext transactionContext;
    private TransactionConfig transactionConfig;

    private MessageQueue prepareQueue;
    private MessageQueue commitQueue;

    private long prepareOffset;
    private long prepareNextOffset;
    private long prepareMessageCount;

    private long commitOffset;
    private long commitNextOffset;

    @Builder.Default
    private long startTime = System.currentTimeMillis();
    /**
     * committed offset list
     * - if the body of message is null
     * - if the body contains no prepare offset
     */
    @Builder.Default
    private List<Long> committedOffsetList = new ArrayList<>();
    // prepareOffset -> commitOffset
    @Builder.Default
    private Map<Long, Long> offsetMap = new HashMap<>();
    /**
     * prepare offset map, waiting for check
     * commitOffset -> Set<PrepareOffset>
     */
    @Builder.Default
    private Map<Long, Set<Long>> commitOffsetMap = new HashMap<>();

    @Builder.Default
    private int invalidMessageCount = 1;
    // count of renewed prepare message
    @Builder.Default
    private int renewMessageCount = 0;
    @Builder.Default
    private int rpcFailureCount = 0;

    public boolean isOffsetValid() {
        return prepareOffset >= 0 && commitOffset >= 0;
    }

    public void initOffset(long commitNextOffset) {
        this.prepareNextOffset = prepareOffset;
        this.prepareMessageCount = prepareOffset;
        this.commitNextOffset = commitNextOffset;
    }

    public boolean isTimeout(long maxTime) {
        long elapsedTime = System.currentTimeMillis() - this.startTime;
        return elapsedTime > maxTime;
    }

    public void addCommittedOffset(long offset) {
        this.committedOffsetList.add(offset);
    }

    public void putOffsetMap(long commitOffset, Set<Long> prepareOffsetMap) {
        this.commitOffsetMap.put(commitOffset, prepareOffsetMap);
    }

    public void linkOffset(long commitOffset, long prepareOffset) {
        this.offsetMap.put(prepareOffset, commitOffset);
    }
}
