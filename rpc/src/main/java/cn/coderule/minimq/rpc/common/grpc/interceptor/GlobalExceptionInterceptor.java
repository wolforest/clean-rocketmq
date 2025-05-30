
package cn.coderule.minimq.rpc.common.grpc.interceptor;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalExceptionInterceptor implements ServerInterceptor {

    @Override
    public <R, W> ServerCall.Listener<R> interceptCall(ServerCall<R, W> call, Metadata headers, ServerCallHandler<R, W> next) {
        final ServerCall<R, W> serverCall = new ClosableServerCall<>(call);
        ServerCall.Listener<R> delegate = next.startCall(serverCall, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<R>(delegate) {
            @Override
            public void onMessage(R message) {
                try {
                    super.onMessage(message);
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            @Override
            public void onCancel() {
                try {
                    super.onCancel();
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            @Override
            public void onComplete() {
                try {
                    super.onComplete();
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            @Override
            public void onReady() {
                try {
                    super.onReady();
                } catch (Throwable e) {
                    closeWithException(e);
                }
            }

            private void closeWithException(Throwable t) {
                Metadata trailers = new Metadata();
                Status status = Status.INTERNAL.withDescription(t.getMessage());
                boolean printLog = true;

                if (t instanceof StatusRuntimeException) {
                    trailers = ((StatusRuntimeException) t).getTrailers();
                    status = ((StatusRuntimeException) t).getStatus();
                    // no error stack for permission denied.
                    if (status.getCode().value() == Status.PERMISSION_DENIED.getCode().value()) {
                        printLog = false;
                    }
                }

                if (printLog) {
                    log.error("grpc server has exception. errorMsg:{}, e:", t.getMessage(), t);
                }

                serverCall.close(status, trailers);
            }
        };
    }

    private static class ClosableServerCall<R, W> extends
        ForwardingServerCall.SimpleForwardingServerCall<R, W> {
        private boolean closeCalled = false;

        ClosableServerCall(ServerCall<R, W> delegate) {
            super(delegate);
        }

        @Override
        public synchronized void close(final Status status, final Metadata trailers) {
            if (closeCalled) {
                return;
            }

            closeCalled = true;
            ClosableServerCall.super.close(status, trailers);
        }
    }
}
