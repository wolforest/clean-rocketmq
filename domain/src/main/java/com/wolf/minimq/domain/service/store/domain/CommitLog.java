package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.bo.MessageContainer;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CommitLog {
    CompletableFuture<EnqueueResult> insert(MessageContainer messageContainer);

    SelectedMappedBuffer select(long offset, int size);
    SelectedMappedBuffer select(long offset);
    List<SelectedMappedBuffer> selectAll(long offset, int size);

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

    long getFlushPosition();
    long getUnFlushedSize();

}
