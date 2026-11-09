package com.wolfmq.store.domain.commitlog.cloud;

import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.common.core.mmap.SelectedMappedBuffer;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueFuture;

public class CloudCommitLog implements CommitLog {
    @Override
    public MappedFileQueue getMappedFileQueue() {
        return null;
    }

    @Override
    public EnqueueFuture insert(MessageBO messageBO) {
        return null;
    }

    @Override
    public MessageBO select(long offset, int size) {
        return null;
    }

    @Override
    public MessageBO select(long offset) {
        return null;
    }

    @Override
    public long getMinOffset() {
        return 0;
    }

    @Override
    public long getMaxOffset() {
        return 0;
    }

    @Override
    public void assignCommitOffset(MessageBO messageBO) {

    }

    @Override
    public long getFlushedOffset() {
        return 0;
    }

    @Override
    public long getUnFlushedSize() {
        return 0;
    }

    @Override
    public InsertResult insert(long offset, byte[] data, int start, int size) {
        return null;
    }

    @Override
    public SelectedMappedBuffer selectBuffer(long offset) {
        return null;
    }

    @Override
    public SelectedMappedBuffer selectBuffer(long offset, int size) {
        return null;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
