package com.wolfmq.store.domain.offset;

import cn.coderule.common.convention.service.Lifecycle;

public class OffsetBootstrap implements Lifecycle {
    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void initialize() throws Exception {
        Lifecycle.super.initialize();
    }
}
