package com.wolfmq.store.domain.commitlog;

import com.wolfmq.common.core.exception.EnqueueException;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueFuture;
import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.core.mmap.SelectedMappedBuffer;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import com.wolfmq.store.domain.commitlog.file.CommitLogManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @renamed from CommitLogFacade to CommitLogManager
 * @renamed from CommitLogManager to CommitLogProxy
 * @renamed from CommitLogProxy to CommitLogFacade
 */
@Getter
@Slf4j
public class CommitLogFacade {
    private final CommitLogManager manager;
    private final OffsetCodec offsetCodec;

    public CommitLogFacade(CommitLogManager manager) {
        this.manager = manager;
        this.offsetCodec = manager.getOffsetCodec();
    }

    public EnqueueFuture insert(MessageBO messageBO) {
        try {
            return manager.selectByMessage(messageBO)
                .insert(messageBO);
        } catch (EnqueueException e) {
            log.error("[CommitLogManager]insert message failed: {}",  e.getMessage());
            return EnqueueFuture.failure(e.getStatus());
        }
    }

    public MessageBO select(long offset, int size) {
        try {
            CommitLog commitLog = manager.selectByOffset(offset);
            return commitLog.select(offset, size);
        } catch (EnqueueException e) {
            log.error("[CommitLogManager]select message failed: {}",  e.getMessage());
            return MessageBO.notFound();
        }
    }

    public MessageBO select(long offset) {
        try {
            CommitLog commitLog = manager.selectByOffset(offset);

            long position = offsetCodec.decode(offset);
            return commitLog.select(position);
        } catch (EnqueueException e) {
            return MessageBO.notFound();
        }
    }

    public InsertResult insert(long offset, byte[] data, int start, int size) {
        CommitLog commitLog = manager.selectByOffset(offset);

        long position = offsetCodec.decode(offset);
        return commitLog.insert(position, data, start, size);
    }

    public SelectedMappedBuffer selectBuffer(long offset) {
        CommitLog commitLog = manager.selectByOffset(offset);

        long position = offsetCodec.decode(offset);
        return commitLog.selectBuffer(position);
    }

    public SelectedMappedBuffer selectBuffer(long offset, int size) {
        CommitLog commitLog = manager.selectByOffset(offset);

        long position = offsetCodec.decode(offset);
        return commitLog.selectBuffer(position, size);
    }

    public long getMinOffset(int shardId) {
        long offset = manager.selectByShard(shardId)
            .getMinOffset();

        return offsetCodec.encode(offset);
    }

    public long getMaxOffset(int shardId) {
        long offset = manager.selectByShard(shardId)
            .getMaxOffset();

        return offsetCodec.encode(offset);
    }

    public long getFlushedOffset(int shardId) {
        return manager.selectByShard(shardId).getFlushedPosition();
    }

    public long getUnFlushedSize(int shardId) {
        return manager.selectByShard(shardId).getUnFlushedSize();
    }

}
