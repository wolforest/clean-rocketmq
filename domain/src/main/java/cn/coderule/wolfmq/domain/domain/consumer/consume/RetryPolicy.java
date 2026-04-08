
package cn.coderule.wolfmq.domain.domain.consumer.consume;

public interface RetryPolicy {
    /**
     * Compute message's next delay duration by specify reconsumeTimes
     *
     * @param reconsumeTimes Message reconsumeTimes
     * @return Message's nextDelayDuration in milliseconds
     */
    long nextDelayDuration(int reconsumeTimes);
}
