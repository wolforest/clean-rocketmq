package com.wolfmq.common.domain.offset.checkpoint.bakeup.model;

public class Checkpoint {
    protected volatile boolean changed = false;

    public boolean hasChange() {
        return changed;
    }

    public void markChanged() {
        changed = true;
    }

    public void markStored() {
        changed = false;
    }
}
