package cn.coderule.minimq.domain.domain.timer.wheel;


import java.io.Serializable;
import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a block of timer log. Format:
 * ┌────────────┬───────────┬────────┬───────────────────┬──────────────────┬───────────┬───────────┬──────────────────────────┬──────────────────────┐
 * │  unit size │  prev pos │  magic │  curr write time  │   delayed time   │ offsetPy  │   sizePy  │ hash code of real topic  │    reserved value    │
 * ├────────────┼───────────┼────────┼───────────────────┼──────────────────┼───────────┼───────────┼──────────────────────────┼──────────────────────┤
 * │   4bytes   │   8bytes  │ 4bytes │      8bytes       │      4bytes      │   8bytes  │   8bytes  │           4bytes         │         8bytes       │
 * └────────────┴───────────┴────────┴───────────────────┴──────────────────┴───────────┴───────────┴──────────────────────────┴──────────────────────┘
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Block implements Serializable {
    public static final short SIZE = 0
            + 4 //size
            + 8 //prev pos
            + 4 //magic value
            + 8 //curr write time, for trace
            + 4 //delayed time, for check
            + 8 //offsetPy
            + 4 //sizePy
            + 4 //hash code of real topic
            + 8;//reserved value, just in case of;
    private final ByteBuffer blockBuffer = ByteBuffer.allocate(SIZE);
    public int size;

    /**
     * the position of task in TimerWheel
     *  - with same delayedTime
     *  - the last inserted
     * The prePos makes a task linkedList
     */
    public long prevPos;
    public int magic;
    public long currWriteTime;
    public int delayedTime;
    public long offsetPy;
    public int sizePy;
    public int hashCodeOfRealTopic;
    public long reservedValue;

    public byte[] bytes() {
        ByteBuffer tmpBuffer = blockBuffer;
        tmpBuffer.clear();
        tmpBuffer.putInt(SIZE); //size
        tmpBuffer.putLong(prevPos); //prev pos ,lastPos
        tmpBuffer.putInt(magic); //magic
        tmpBuffer.putLong(currWriteTime); //currWriteTime,tmpWriteTimeMs
        tmpBuffer.putInt(delayedTime); //delayTime,(int) (delayedTime - tmpWriteTimeMs)
        tmpBuffer.putLong(offsetPy); //offset
        tmpBuffer.putInt(sizePy); //size
        tmpBuffer.putInt(hashCodeOfRealTopic); //hashcode of real topic,metricManager.hashTopicForMetrics(realTopic)
        tmpBuffer.putLong(0); //reserved value, just set to 0 now
        return tmpBuffer.array();
    }
}
