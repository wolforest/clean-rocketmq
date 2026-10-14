package cn.coderule.wolfmq.broker.server.grpc.interceptor;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.wolfmq.domain.core.enums.code.InvalidCode;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.rpc.common.grpc.RequestPipeline;
import cn.coderule.wolfmq.rpc.common.grpc.core.constants.GrpcConstants;
import com.google.protobuf.GeneratedMessage;
import io.grpc.Context;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class PipelineExecutor {
    private final RequestPipeline pipeline;

    public PipelineExecutor(RequestPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public  <T> void execute(RequestContext context, T request) {
        if (request instanceof GeneratedMessage) {
            pipeline.execute(
                context,
                GrpcConstants.METADATA.get(Context.current()),
                (GeneratedMessage) request
            );

            validateContext(context);
            return;
        }

        log.error("[BUG] grpc request pipeline is not executed.");
    }

    private void validateContext(RequestContext context) {
        if (StringUtil.isBlank(context.getClientID())) {
            throw new InvalidRequestException(InvalidCode.CLIENT_ID_REQUIRED, "clientID is required.");
        }
    }
}
