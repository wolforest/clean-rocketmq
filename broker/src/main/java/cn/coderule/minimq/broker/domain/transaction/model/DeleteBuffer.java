package cn.coderule.minimq.broker.domain.transaction.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DeleteBuffer {
    private final ConcurrentMap<Integer, OffsetQueue> queueMap;

    public DeleteBuffer() {
        this.queueMap = new ConcurrentHashMap<>();
    }
}
