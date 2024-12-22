package com.wolf.minimq.store.infra.file;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.lang.concurrent.ServiceThread;

public class AllocateMappedFileService extends ServiceThread implements Lifecycle {
    @Override
    public String getServiceName() {
        return AllocateMappedFileService.class.getSimpleName();
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

    @Override
    public void run() {

    }
}
