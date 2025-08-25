package cn.coderule.minimq.domain.core.enums.consume;

/**
 * the mode of the start consume offset in queue
 * replace by {@link ConsumeStrategy}
 */
@Deprecated
public class ConsumeInitMode {
    /**
     * consume from min offset in queue
     * replace by {@link ConsumeStrategy#CONSUME_FROM_MIN_OFFSET}
     */
    public static final int MIN = 0;

    /**
     * consume form the latest offset in queue
     * replace by {@link ConsumeStrategy#CONSUME_FROM_LATEST}
     */
    public static final int MAX = 1;
}
