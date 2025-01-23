package com.wolf.minimq.broker.server.grpc;

import com.wolf.common.convention.service.Lifecycle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcServer implements Lifecycle {


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
        return State.RUNNING;
    }
}
