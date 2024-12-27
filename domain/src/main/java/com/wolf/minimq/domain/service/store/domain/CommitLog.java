package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.dto.MessageContext;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.dto.SelectedMappedBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CommitLog {
    CompletableFuture<EnqueueResult> insert(MessageContext messageContext);

    SelectedMappedBuffer select(long offset, int size);
    SelectedMappedBuffer select(long offset);
    List<SelectedMappedBuffer> selectAll(long offset, int size);

    long getMinOffset();
    long getMaxOffset();

    long getFlushPosition();
    long getUnFlushedSize();

}
