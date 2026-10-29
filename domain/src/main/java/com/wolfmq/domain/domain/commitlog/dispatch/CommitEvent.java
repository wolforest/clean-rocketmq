package com.wolfmq.domain.domain.commitlog;

import com.wolfmq.domain.domain.message.MessageBO;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
