package cn.coderule.minimq.domain.domain.meta.order;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConsumeOrder implements Serializable {
    private static final long CLEAN_SPAN_FROM_LAST = 24 * 3600 * 1000;

    private final ConcurrentMap<String, ConcurrentMap<Integer, OrderInfo>> orderMap;

    public ConsumeOrder() {
        this.orderMap = new ConcurrentHashMap<>();
    }

    public boolean isLocked(OrderRequest request) {
        return false;
    }

    public void update(OrderRequest request) {
    }

    public void clearLock() {

    }
}
