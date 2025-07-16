package cn.coderule.minimq.domain.domain.timer;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * timer task to be scheduled, build from ConsumeQueue and CommitLog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimerEvent implements Serializable {

    /**
     * commitLog offset
     */
    private long commitLogOffset;
    /**
     * size of message in the commitLog
     */
    private int messageSize;
    /**
     * delayTime of message, stored in message property map
     */
    private long delayTime;
    /**
     * magic code, always equals TimerMessageAccepter.MAGIC_DEFAULT (1)
     */
    private int magic;

    /**
     * enqueue timestamp (ms)
     */
    private long enqueueTime;
    /**
     * timer task related msg
     */
    private MessageBO messageBO;

    //optional would be a good choice, but it relies on JDK 8
    private CountDownLatch latch;

    private boolean released;

    //whether the operation is successful
    private boolean success;

    private Set<String> deleteList;

    @Override
    public String toString() {
        return "TimerRequest{" +
            "offsetPy=" + commitLogOffset +
            ", sizePy=" + messageSize +
            ", delayTime=" + delayTime +
            ", enqueueTime=" + enqueueTime +
            ", magic=" + magic +
            ", msg=" + messageBO +
            ", latch=" + latch +
            ", released=" + released +
            ", succ=" + success +
            ", deleteList=" + deleteList +
            '}';
    }
}
