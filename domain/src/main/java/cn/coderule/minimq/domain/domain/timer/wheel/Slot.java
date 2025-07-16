package cn.coderule.minimq.domain.domain.timer.wheel;

/**
 * Represents a slot of timing wheel. Format:
 * ┌────────────┬───────────┬───────────┬───────────┬───────────┐
 * │delayed time│ first pos │ last pos  │    num    │   magic   │
 * ├────────────┼───────────┼───────────┼───────────┼───────────┤
 * │   8bytes   │   8bytes  │  8bytes   │   4bytes  │   4bytes  │
 * └────────────┴───────────┴───────────┴───────────┴───────────┘
 */
public class Slot {
    public static final short SIZE = 32;
    public final long timeMs; //delayed time
    public final long firstPos;
    public final long lastPos;
    public final int num;
    public final int magic; //no use now, just keep it

    public Slot(long timeMs, long firstPos, long lastPos) {
        this.timeMs = timeMs;
        this.firstPos = firstPos;
        this.lastPos = lastPos;
        this.num = 0;
        this.magic = 0;
    }

    public Slot(long timeMs, long firstPos, long lastPos, int num, int magic) {
        this.timeMs = timeMs;
        this.firstPos = firstPos;
        this.lastPos = lastPos;
        this.num = num;
        this.magic = magic;
    }
}
