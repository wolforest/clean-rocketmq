package cn.coderule.minimq.domain.domain.timer.wheel;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a slot of timing wheel. Format:
 * ┌────────────┬───────────┬───────────┬───────────┬───────────┐
 * │delayed time│ first pos │ last pos  │    num    │   magic   │
 * ├────────────┼───────────┼───────────┼───────────┼───────────┤
 * │   8bytes   │   8bytes  │  8bytes   │   4bytes  │   4bytes  │
 * └────────────┴───────────┴───────────┴───────────┴───────────┘
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Slot implements Serializable {
    public static final short SIZE = 32;
    public long timeMs; //delayed time
    public long firstPos;
    public long lastPos;
    public int num;
    public int magic; //useless now, just keep it

    public Slot(long timeMs, long firstPos, long lastPos) {
        this.timeMs = timeMs;
        this.firstPos = firstPos;
        this.lastPos = lastPos;
        this.num = 0;
        this.magic = 0;
    }

}
