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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckContext implements Serializable {
    private TransactionContext transactionContext;
    private TransactionConfig transactionConfig;

    private MessageQueue prepareQueue;
    private MessageQueue commitQueue;

    private long prepareStartOffset;
    private long prepareOffset;
    private long prepareNextOffset;

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
    private int invalidPrepareMessageCount = 1;
    // count of renewed prepare message
    @Builder.Default
    private int renewMessageCount = 0;
    @Builder.Default
    private int rpcFailureCount = 0;

    public boolean isOffsetValid() {
        boolean status = prepareStartOffset >= 0 && commitOffset >= 0;
        if (!status) {
            return false;
        }

        log.error("invalid offset for checking: prepareQueue={}, prepareOffset={}, commitOffset={}",
            prepareQueue, prepareStartOffset, commitOffset);

        return true;
    }

    public void increaseInvalidPrepareMessageCount() {
        this.invalidPrepareMessageCount++;
    }

    public void setPrepareOffset(long counter) {
        this.prepareOffset = counter;
        this.prepareNextOffset = counter;
    }

    public void increasePrepareCounter() {
        this.prepareOffset++;
        this.prepareNextOffset = prepareOffset;
    }

    public void initOffset(long commitNextOffset) {
        this.prepareNextOffset = prepareStartOffset;
        this.prepareOffset = prepareStartOffset;
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

    public boolean containsPrepareOffset(long prepareOffset) {
        return this.offsetMap.containsKey(prepareOffset);
    }

    public void removePrepareOffset(long prepareOffset) {
        Long commitOffset = this.offsetMap.remove(prepareOffset);
        if (commitOffset == null) {
            return;
        }

        Set<Long> prepareOffsetSet = this.commitOffsetMap.get(commitOffset);
        if (null == prepareOffsetSet) {
            return;
        }

        prepareOffsetSet.remove(prepareOffset);
        if (!prepareOffsetSet.isEmpty()) {
            return;
        }

        this.commitOffsetMap.remove(commitOffset);
        this.committedOffsetList.add(commitOffset);
    }
}
