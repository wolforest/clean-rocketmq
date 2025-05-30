
package cn.coderule.minimq.rpc.common.grpc.interceptor;

import cn.coderule.minimq.rpc.common.grpc.core.constants.GrpcConstants;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class ContextInterceptor implements ServerInterceptor {

    @Override
    public <R, W> ServerCall.Listener<R> interceptCall(
        ServerCall<R, W> call,
        Metadata headers,
        ServerCallHandler<R, W> next
    ) {
        Context context = Context.current().withValue(GrpcConstants.METADATA, headers);
        return Contexts.interceptCall(context, call, headers, next);
    }
}
