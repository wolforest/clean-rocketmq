package com.wolfmq.broker.server.grpc.interceptor;

import cn.coderule.common.util.lang.string.StringUtil;
import com.wolfmq.domain.core.constant.code.InvalidCode;
import com.wolfmq.domain.core.exception.InvalidRequestException;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.rpc.common.grpc.RequestPipeline;
import com.wolfmq.rpc.common.grpc.core.constants.GrpcConstants;
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
