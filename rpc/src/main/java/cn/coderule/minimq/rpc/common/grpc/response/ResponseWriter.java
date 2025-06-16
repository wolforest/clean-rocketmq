
package cn.coderule.minimq.rpc.common.grpc.response;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResponseWriter {

    protected static final Object INSTANCE_CREATE_LOCK = new Object();
    protected static volatile ResponseWriter instance;

    public static ResponseWriter getInstance() {
        if (instance != null) {
            return instance;
        }

        synchronized (INSTANCE_CREATE_LOCK) {
            if (instance == null) {
                instance = new ResponseWriter();
            }
        }
        return instance;
    }

    public <T> void write(StreamObserver<T> observer, final T response) {
        if (writeResponse(observer, response)) {
            observer.onCompleted();
        }
    }

    public <T> boolean writeResponse(StreamObserver<T> observer, final T response) {
        if (null == response) {
            return false;
        }
        log.debug("start to write response. response: {}", response);
        if (isCancelled(observer)) {
            log.warn("client has cancelled the request. response to write: {}", response);
            return false;
        }

        try {
            observer.onNext(response);
        } catch (StatusRuntimeException e) {
            if (Status.CANCELLED.equals(e.getStatus())) {
                log.warn("client has cancelled the request. response to write: {}", response);
                return false;
            }
            throw e;
        }
        return true;
    }

    public <T> boolean isCancelled(StreamObserver<T> observer) {
        if (observer instanceof ServerCallStreamObserver<T> serverCallStreamObserver) {
            return serverCallStreamObserver.isCancelled();
        }
        return false;
    }
}

