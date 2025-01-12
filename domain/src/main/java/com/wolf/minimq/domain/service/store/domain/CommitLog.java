package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CommitLog {
    MappedFileQueue getMappedFileQueue();

    CompletableFuture<EnqueueResult> insert(MessageBO messageBO);

    MessageBO select(long offset, int size);

    MessageBO select(long offset);

    /**
     * get min offset, which is the start offset of the first mappedFile
     * @return minOffset
     */
    long getMinOffset();

    /**
     * get max offset, which is
     *  - the write position in default setting
     *  - or the commit position if enable write cache
     * @return maxOffset
     */
    long getMaxOffset();

    /**
     * get flushed offset
     * @return flushedPosition of the mappedFileQueue
     */
    long getFlushedOffset();

    /**
     * get unFlushed size
     * @return unFlushedSize of the mappedFileQueue
     */
    long getUnFlushedSize();

}
