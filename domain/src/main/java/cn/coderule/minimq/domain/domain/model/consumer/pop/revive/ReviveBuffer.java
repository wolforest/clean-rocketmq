package cn.coderule.minimq.domain.domain.model.consumer.pop.revive;

import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
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
    private final HashMap<String, PopCheckPoint> ackMap;
    private ArrayList<PopCheckPoint> sortedList;

    /**
     * the initial offset of revive topic queue
     */
    private long initialOffset;

    /**
     * the working offset
     */
    private long offset;

    private int noMsgCount;

    private long firstReviveTime;

    private long startTime;
    /**
     * the max deliverTime of messageExt,
     * which bulk pulled from consume queue
     */
    private long maxDeliverTime;

    public ReviveBuffer() {
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

}
