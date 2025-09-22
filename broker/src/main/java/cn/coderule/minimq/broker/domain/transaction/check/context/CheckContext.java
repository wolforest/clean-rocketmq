package cn.coderule.minimq.broker.domain.transaction.check.context;

import cn.coderule.minimq.domain.config.business.TransactionConfig;
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

    private long startPrepareOffset;
    private long prepareOffset;
    private long nextPrepareOffset;

    private long operationOffset;
    private long nextOperationOffset;

    @Builder.Default
    private long startTime = System.currentTimeMillis();
    /**
     * operation offset list
     * - if the body of message is null
     * - if the body contains no prepare offset
     */
    @Builder.Default
    private List<Long> operationOffsetList = new ArrayList<>();
    // prepareOffset -> commitOffset
    @Builder.Default
    private Map<Long, Long> offsetMap = new HashMap<>();
    /**
     * prepare offset map, waiting for check
     * commitOffset -> Set<PrepareOffset>
     */
    @Builder.Default
    private Map<Long, Set<Long>> operationOffsetMap = new HashMap<>();

    @Builder.Default
    private int invalidPrepareMessageCount = 1;
    // count of renewed prepare message
    @Builder.Default
    private int renewMessageCount = 0;
    @Builder.Default
    private int rpcFailureCount = 0;

    public boolean isOffsetValid() {
        boolean status = startPrepareOffset >= 0 && operationOffset >= 0;
        if (!status) {
            return false;
        }

        log.error("invalid offset for checking: prepareQueue={}, prepareOffset={}, commitOffset={}",
            prepareQueue, startPrepareOffset, operationOffset);

        return true;
    }

    public void increaseInvalidPrepareMessageCount() {
        this.invalidPrepareMessageCount++;
    }

    public void setPrepareOffset(long counter) {
        this.prepareOffset = counter;
        this.nextPrepareOffset = counter;
    }

    public void increasePrepareOffset() {
        this.prepareOffset++;
        this.nextPrepareOffset = prepareOffset;
    }

    public void initOffset(long commitNextOffset) {
        this.nextPrepareOffset = startPrepareOffset;
        this.prepareOffset = startPrepareOffset;
        this.nextOperationOffset = commitNextOffset;
    }

    public boolean isTimeout(long maxTime) {
        long elapsedTime = System.currentTimeMillis() - this.startTime;
        return elapsedTime > maxTime;
    }

    public void addCommittedOffset(long offset) {
        this.operationOffsetList.add(offset);
    }

    public void putOffsetMap(long commitOffset, Set<Long> prepareOffsetMap) {
        this.operationOffsetMap.put(commitOffset, prepareOffsetMap);
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

        Set<Long> prepareOffsetSet = this.operationOffsetMap.get(commitOffset);
        if (null == prepareOffsetSet) {
            return;
        }

        prepareOffsetSet.remove(prepareOffset);
        if (!prepareOffsetSet.isEmpty()) {
            return;
        }

        this.operationOffsetMap.remove(commitOffset);
        this.operationOffsetList.add(commitOffset);
    }
}
