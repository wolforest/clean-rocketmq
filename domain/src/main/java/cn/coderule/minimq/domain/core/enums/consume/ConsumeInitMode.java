package cn.coderule.minimq.domain.core.enums.consume;

/**
 * the mode of the start consume offset in queue
 */
public class ConsumeInitMode {
    /**
     * consume from min offset in queue
     */
    public static final int MIN = 0;

    /**
     * consume form the latest offset in queue
     */
    public static final int MAX = 1;
}
