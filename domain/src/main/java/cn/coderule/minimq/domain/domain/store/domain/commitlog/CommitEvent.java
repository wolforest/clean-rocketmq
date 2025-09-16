package cn.coderule.minimq.domain.domain.store.domain.commitlog;

import cn.coderule.minimq.domain.domain.message.MessageBO;
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
    private MessageBO messageBO;

    // filter info
    private byte[] bitMap;

    public static CommitEvent of(MessageBO messageBO) {
        return CommitEvent.builder()
            .messageBO(messageBO)
            .build();
    }
}
