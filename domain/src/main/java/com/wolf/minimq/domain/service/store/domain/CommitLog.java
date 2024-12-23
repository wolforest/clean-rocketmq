package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.vo.MessageContext;
import com.wolf.minimq.domain.vo.EnqueueResult;
import com.wolf.minimq.domain.vo.SelectedMappedBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CommitLog {
    CompletableFuture<EnqueueResult> append(MessageContext messageContext);

    SelectedMappedBuffer select(long offset, int size);
    SelectedMappedBuffer select(long offset);
    List<SelectedMappedBuffer> selectAll(long offset, int size);


}
