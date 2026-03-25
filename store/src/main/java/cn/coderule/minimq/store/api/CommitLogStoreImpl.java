package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.SelectedMappedBuffer;
import cn.coderule.minimq.domain.domain.store.api.CommitLogStore;
import cn.coderule.minimq.store.domain.commitlog.log.CommitLogManager;

public class CommitLogStoreImpl implements CommitLogStore {
    private final CommitLogManager commitLogManager;

    public CommitLogStoreImpl(CommitLogManager commitLogManager) {
        this.commitLogManager = commitLogManager;
    }
    @Override
    public SelectedMappedBuffer select(long offset) {
        return commitLogManager.selectBuffer(offset);
    }

    @Override
    public InsertResult insert(long offset, byte[] data, int start, int size) {
        return commitLogManager.insert(offset, data, start, size);
    }

    @Override
    public long getMinOffset(int shardId) {
        return commitLogManager.getMinOffset(shardId);
    }

    @Override
    public long getMaxOffset(int shardId) {
        return commitLogManager.getMaxOffset(shardId);
    }

    @Override
    public long getFlushedOffset(int shardId) {
        return commitLogManager.getFlushedOffset(shardId);
    }

    @Override
    public long getUnFlushedSize(int shardId) {
        return commitLogManager.getUnFlushedSize(shardId);
    }

}
