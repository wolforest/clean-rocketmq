package com.wolf.minimq.broker.server.grpc;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.lang.exception.SystemException;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcServer implements Lifecycle {
    private final Server server;
    private final long timeout;

    private NettyServerBuilder serverBuilder;

    public GrpcServer(Server server, long timeout) {
        this.server = server;
        this.timeout = timeout;
    }

    @Override
    public void start() {
        try {
            this.server.start();
        } catch (Exception e) {
            log.error("start grpc server error", e);
            throw new SystemException("start grpc server error");
        }
    }

    @Override
    public void shutdown() {
        try {
            this.server.shutdown()
                .awaitTermination(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("shutdown grpc server error", e);
        }
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
