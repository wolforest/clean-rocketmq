package cn.coderule.minimq.domain.domain.consumer.revive;

import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopKeyBuilder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;

@Data
public class ReviveBuffer implements Serializable {
    /**
     * data model
     *  key: PopKeyBuilder.buildReviveKey
     *      = topic + group + queueId + startOffset + popTime
     *  value: PopCheckPoint
     * put by:
     *  - PopReviveThread.mockCKForAck
     *    if brokerConfig.enableSkipLongAwaitingAck == true
     *  - PopReviveThread.parseCheckPointMessage
     */
    private final HashMap<String, PopCheckPoint> checkPointMap;
    /**
     * ack related checkpoint map
     */
    private final HashMap<String, PopCheckPoint> ackMap;
    /**
     * the sorted list of PopCheckPoint, sorted by reviveOffset
     */
    private ArrayList<PopCheckPoint> sortedList;

    /**
     * the initial offset of revive topic queue
     */
    private final long initialOffset;

    /**
     * the working offset
     */
    private long offset;

    /**
     * the count of request which no message found
     */
    private int noMsgCount;

    /**
     * the first reviveTime of PopCheckPoint,
     * which bulk pulled from revive queue
     */
    private long firstReviveTime;

    /**
     * the start time of revive,
     * also the time of ReviveBuffer was created
     */
    private long startTime;
    /**
     * the max deliverTime of messageExt,
     * which bulk pulled from consume queue
     */
    private long maxDeliverTime;

    public ReviveBuffer(long initialOffset) {
        this.initialOffset = initialOffset;
        this.offset = initialOffset + 1;

        this.startTime = System.currentTimeMillis();
        this.noMsgCount = 0;
        this.firstReviveTime = 0;
        this.maxDeliverTime = 0;

        this.ackMap = new HashMap<>();
        this.checkPointMap = new HashMap<>();
    }

    public ArrayList<PopCheckPoint> getSortedList() {
        if (sortedList != null) {
            return sortedList;
        }

        sortedList = new ArrayList<>(checkPointMap.values());
        sortedList.sort(
            (o1, o2) -> (int) (o1.getReviveOffset() - o2.getReviveOffset())
        );

        return sortedList;
    }

    public void increaseNoMsgCount() {
        this.noMsgCount++;
    }

    public void addCheckPoint(PopCheckPoint point) {
        checkPointMap.put(
            PopKeyBuilder.buildKey(point),
            point
        );
    }

    public void addAck(String key, PopCheckPoint point) {
        ackMap.put(key, point);
    }

    public PopCheckPoint getCheckPoint(String mergeKey) {
        return checkPointMap.get(mergeKey);
    }

    public void mergeAckMap() {
        checkPointMap.putAll(ackMap);
    }

}
