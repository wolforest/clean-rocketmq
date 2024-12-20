package com.wolf.minimq.store.domain.commitlog;

import com.wolf.minimq.domain.context.MessageContext;
import com.wolf.minimq.domain.service.store.CommitLog;
import com.wolf.minimq.domain.vo.AppendMessageResult;
import com.wolf.minimq.domain.vo.SelectedMappedBuffer;
import java.util.List;

public class DefaultCommitLog implements CommitLog {
    @Override
    public AppendMessageResult append(MessageContext messageContext) {
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

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return null;
    }
}
