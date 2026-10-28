package com.wolfmq.domain.core.kv;

import cn.coderule.common.convention.service.Lifecycle;

public interface KVDatabase extends Lifecycle, AutoCloseable {
    String getName();
    KVTable openTable(String name);
    boolean containsTable(String name);
}
