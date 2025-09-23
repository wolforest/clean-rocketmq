package cn.coderule.minimq.broker.domain.transaction.check.context;

import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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

    @Builder.Default
    private long startTime = System.currentTimeMillis();

    private MessageQueue prepareQueue;
    private MessageQueue operationQueue;

    /**
     * @rocketmq original name: counter
     */
    private long prepareCounter;
    /**
     * @rocketmq original name: halfOffset
     */
    private long prepareOffset;
    /**
     * @rocketmq original name: newOffset
     */
    private long nextPrepareOffset;

    /**
     * @rocketmq original name: opOffset
     */
    private long operationOffset;
    /**
     * @rocketmq original name: nextOpOffset
     */
    private long nextOperationOffset;

    /**
     * @rocketmq original name: doneOpOffset
     *  operation offset list
     * - if the body of message is null
     * - if the body contains no prepare offset
     */
    @Builder.Default
    private List<Long> operationOffsetList = new ArrayList<>();
    /**
     * @rocketmq original name: removeMap
     * prepare offset -> commit offset
     */
    @Builder.Default
    private Map<Long, Long> offsetMap = new HashMap<>();
    /**
     * @rocketmq original name: opMsgMap
     * prepare offset map, waiting for check
     * operationOffset -> Set<PrepareOffset>
     */
    @Builder.Default
    private Map<Long, Set<Long>> operationMap = new HashMap<>();

    @Builder.Default
    private int invalidPrepareMessageCount = 1;
    // count of renewed prepare message
    @Builder.Default
    private int renewMessageCount = 0;
    @Builder.Default
    private int rpcFailureCount = 0;

    public boolean isOffsetValid() {
        if (prepareCounter < 0 && operationOffset < 0) {
            return false;
        }

        log.error("invalid checking offset: prepareOffset={}, operationOffset={}",
            prepareOffset, operationOffset);
        return true;
    }

    public void increaseInvalidPrepareMessageCount() {
        this.invalidPrepareMessageCount++;
    }

    public void setPrepareCounter(long offset) {
        this.prepareCounter = offset;
        this.nextPrepareOffset = prepareCounter;
    }

    public void increasePrepareCounter() {
        this.prepareCounter++;
        this.nextPrepareOffset = prepareCounter;
    }

    public void initOffset(long nextOperationOffset) {
        this.nextPrepareOffset = prepareOffset;
        this.prepareCounter = prepareOffset;
        this.nextOperationOffset = nextOperationOffset;
    }

    public boolean isTimeout(long maxTime) {
        long elapsedTime = System.currentTimeMillis() - this.startTime;
        return elapsedTime > maxTime;
    }

    public void addOperationOffset(long offset) {
        this.operationOffsetList.add(offset);
    }

    public void putOffsetMap(long prepareOffset, Set<Long> prepareOffsetMap) {
        this.operationMap.put(prepareOffset, prepareOffsetMap);
    }

    public void linkOffset(long prepareOffset, long operationOffset) {
        this.offsetMap.put(prepareOffset, operationOffset);
    }

    public boolean containsPrepareOffset(long prepareOffset) {
        return this.offsetMap.containsKey(prepareOffset);
    }

    public void removePrepareOffset(long prepareOffset) {
        Long commitOffset = this.offsetMap.remove(prepareOffset);
        if (commitOffset == null) {
            return;
        }

        Set<Long> prepareOffsetSet = this.operationMap.get(commitOffset);
        if (null == prepareOffsetSet) {
            return;
        }

        prepareOffsetSet.remove(prepareOffset);
        if (!prepareOffsetSet.isEmpty()) {
            return;
        }

        this.operationMap.remove(commitOffset);
        this.operationOffsetList.add(commitOffset);
    }

    public long calculateOperationOffset() {
        Collections.sort(this.operationOffsetList);
        long tmpOffset = this.operationOffset;

        for (Long offset : this.operationOffsetList) {
            if (offset != tmpOffset) {
                break;
            }

            tmpOffset++;
        }

        return tmpOffset;
    }
}
