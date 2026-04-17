package cn.coderule.wolfmq.domain.domain.store.domain.commitlog;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Async CommitLog dispatching core object
 * created By ReputMessageService
 * dispatch to :
 *     1. CommitLogDispatcherBuildConsumeQueue
 *          -> ConsumeQueueStore.putMessagePositionInfoWrapper()
 *     2. CommitLogDispatcherBuildIndex
 *          -> IndexService.buildIndex()
 *     3. CommitLogDispatcherCompaction
 *          -> CompactionService.putRequest()
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommitEvent implements Serializable {
    private static final int DEFAULT_SHARD_ID = -1;

    private int shardId = DEFAULT_SHARD_ID;
    private MessageBO messageBO;

    // filter info
    private byte[] bitMap;

    public static CommitEvent of(MessageBO messageBO) {
        return of(messageBO, DEFAULT_SHARD_ID);
    }

    public static CommitEvent of(MessageBO messageBO, int shardId) {
        return CommitEvent.builder()
            .messageBO(messageBO)
            .shardId(shardId)
            .build();
    }
}
