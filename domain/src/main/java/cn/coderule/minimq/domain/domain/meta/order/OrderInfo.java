package cn.coderule.minimq.domain.domain.meta.order;

import com.alibaba.fastjson2.annotation.JSONField;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo implements Serializable {
    private long popTime;
    /**
     * the invisibleTime when pop message
     */
    @JSONField(name = "i")
    private Long invisibleTime;
    /**
     * offset
     * offsetList[0] is the queue offset of message
     * offsetList[i] (i > 0) is the distance between current message and offsetList[0]
     */
    @JSONField(name = "o")
    private List<Long> offsetList;
    /**
     * next visible timestamp for message
     * key: message queue offset
     */
    @JSONField(name = "ot")
    private Map<Long, Long> offsetNextVisibleTime;
    /**
     * message consumed count for offset
     * key: message queue offset
     */
    @JSONField(name = "oc")
    private Map<Long, Integer> offsetConsumedCount;
    /**
     * last consume timestamp
     */
    @JSONField(name = "l")
    private long lastConsumeTimestamp;
    /**
     * commit offset bit
     */
    @JSONField(name = "cm")
    private long commitOffsetBit;
    @JSONField(name = "a")
    private String attemptId;

}
