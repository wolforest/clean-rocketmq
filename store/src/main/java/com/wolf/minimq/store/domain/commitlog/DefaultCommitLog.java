package com.wolf.minimq.store.domain.commitlog;

import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.vo.EnqueueResult;
import com.wolf.minimq.domain.vo.MessageContext;
import com.wolf.minimq.domain.vo.SelectedMappedBuffer;
import java.util.List;

public class DefaultCommitLog implements CommitLog {
    @Override
    public EnqueueResult append(MessageContext messageContext) {
        return null;
    }

    @Override
    public SelectedMappedBuffer select(long offset, int size) {
        return null;
    }

    @Override
    public SelectedMappedBuffer select(long offset) {
        return null;
    }

    @Override
    public List<SelectedMappedBuffer> selectAll(long offset, int size) {
        return List.of();
    }
}
