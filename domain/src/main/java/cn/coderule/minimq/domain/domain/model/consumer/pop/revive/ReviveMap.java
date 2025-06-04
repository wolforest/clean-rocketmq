package cn.coderule.minimq.domain.domain.model.consumer.pop.revive;

import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;

@Data
public class ReviveMap implements Serializable {
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
    private final HashMap<String, PopCheckPoint> map = new HashMap<>();
    private ArrayList<PopCheckPoint> sortedList;

    private long initialOffset;
    private long endTime;
    private long newOffset;

    public ArrayList<PopCheckPoint> getSortedList() {
        if (sortedList != null) {
            return sortedList;
        }

        sortedList = new ArrayList<>(map.values());
        sortedList.sort(
            (o1, o2) -> (int) (o1.getReviveOffset() - o2.getReviveOffset())
        );

        return sortedList;
    }
}
