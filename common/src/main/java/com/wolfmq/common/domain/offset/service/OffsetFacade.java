package com.wolfmq.common.domain.offset.service;

import java.util.Map;

public class OffsetFacade implements CommitOffsetService, QueueOffsetService, DispatchOffsetService {
    private final CommitOffsetService commitOffsetService;
    private final QueueOffsetService queueOffsetService;
    private final DispatchOffsetService dispatchOffsetService;

    public OffsetFacade(
        CommitOffsetService commitOffsetService,
        QueueOffsetService queueOffsetService,
        DispatchOffsetService dispatchOffsetService
    ) {
        this.commitOffsetService = commitOffsetService;
        this.queueOffsetService = queueOffsetService;
        this.dispatchOffsetService = dispatchOffsetService;
    }

    @Override
    public long getMaxCommitOffset() {
        return commitOffsetService.getMaxCommitOffset();
    }

    @Override
    public long assignCommitOffset() {
        return commitOffsetService.assignCommitOffset();
    }

    @Override
    public boolean updateMasterOffset(int shardId, long offset) {
        return commitOffsetService.updateMasterOffset(shardId, offset);
    }

    @Override
    public boolean updateMasterOffsetMap(Map<Integer, Long> offsetMap) {
        return commitOffsetService.updateMasterOffsetMap(offsetMap);
    }

    @Override
    public long getMaxQueueOffset() {
        return queueOffsetService.getMaxQueueOffset();
    }

    @Override
    public long assignQueueOffset(String topic, int queueId) {
        return queueOffsetService.assignQueueOffset(topic, queueId);
    }

    @Override
    public boolean updateDispatchedOffset(int shardId, long offset) {
        return dispatchOffsetService.updateDispatchedOffset(shardId, offset);
    }

    @Override
    public boolean updateIndexedOffset(int shardId, long offset) {
        return dispatchOffsetService.updateIndexedOffset(shardId, offset);
    }

    @Override
    public boolean updateQueueCommitOffset(String topic, int queueId, int shardId, long offset) {
        return dispatchOffsetService.updateQueueCommitOffset(topic, queueId, shardId, offset);
    }
}
