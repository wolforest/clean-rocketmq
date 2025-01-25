package com.wolf.minimq.broker.server.grpc.common;

import com.wolf.minimq.broker.server.model.RequestContext;
import io.grpc.stub.StreamObserver;
import lombok.Data;

@Data
public class GrpcTask<V, T> implements Runnable {
    protected final Runnable runnable;
    protected final RequestContext context;
    protected final V request;
    protected final T executeRejectResponse;
    protected final StreamObserver<T> streamObserver;

    public GrpcTask(Runnable runnable, RequestContext context, V request, StreamObserver<T> streamObserver,
        T executeRejectResponse) {
        this.runnable = runnable;
        this.context = context;
        this.streamObserver = streamObserver;
        this.request = request;
        this.executeRejectResponse = executeRejectResponse;
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
