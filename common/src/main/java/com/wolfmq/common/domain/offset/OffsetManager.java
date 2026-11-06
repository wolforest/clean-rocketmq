package com.wolfmq.common.domain.offset;

import com.wolfmq.common.domain.offset.dto.OffsetRequest;
import com.wolfmq.common.domain.offset.dto.OffsetResult;
import java.util.Map;

public interface OffsetManager {
    long assignCommitOffset();
    long assignQueueOffset(String topic, int queueId);

    long getMaxCommitOffset();
    long getMaxQueueOffset();

    boolean updateDispatchedOffset(int shardId, long offset);
    boolean updateMasterOffset(int shardId, long offset);
    boolean updateMasterOffsetMap(Map<Integer, Long> offsetMap);
    boolean updateIndexedOffset(int shardId, long offset);
    boolean updateQueueCommitOffset(int shardId, long offset);

    OffsetResult getConsumeOffset(OffsetRequest request);
    void updateConsumeOffset(OffsetRequest request);

    OffsetResult getTagConsumeOffset(OffsetRequest request);
    void updateTagConsumeOffset(OffsetRequest request);

}
