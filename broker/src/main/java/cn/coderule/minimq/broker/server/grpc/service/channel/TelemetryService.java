package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.broker.grpc.ContextStreamObserver;
import io.grpc.stub.StreamObserver;

public class TelemetryService {

    public ContextStreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver) {
        return new ContextStreamObserver<>() {

            @Override
            public void onNext(RequestContext ctx, TelemetryCommand value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }
}
