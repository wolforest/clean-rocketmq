package cn.coderule.wolfmq.rpc.broker.grpc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContextStreamObserverTest {

    @Test
    void interfaceMethods_ShouldBeDefined() {
        ContextStreamObserver<String> observer = new ContextStreamObserver<>() {
            @Override
            public void onNext(cn.coderule.wolfmq.domain.domain.cluster.RequestContext ctx, String value) {}
            @Override
            public void onError(Throwable t) {}
            @Override
            public void onCompleted() {}
        };

        assertNotNull(observer);
        assertDoesNotThrow(() -> observer.onNext(null, "test"));
        assertDoesNotThrow(() -> observer.onError(null));
        assertDoesNotThrow(() -> observer.onCompleted());
    }
}