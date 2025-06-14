package cn.coderule.minimq.broker.server.grpc.service;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.rpc.common.core.enums.ChannelProtocolType;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.RequestPipeline;
import cn.coderule.minimq.rpc.common.grpc.core.constants.GrpcConstants;
import com.google.protobuf.GeneratedMessage;
import io.grpc.Context;
import io.grpc.Metadata;
import java.util.concurrent.TimeUnit;

public class ContextInitPipeline implements RequestPipeline {
    @Override
    public void execute(RequestContext context, Metadata headers, GeneratedMessage request) {
        Context ctx = Context.current();
        context.setRequestTime(System.currentTimeMillis())
            .setLocalAddress(getDefaultStringMetadataInfo(headers, GrpcConstants.LOCAL_ADDRESS))
            .setRemoteAddress(getDefaultStringMetadataInfo(headers, GrpcConstants.REMOTE_ADDRESS))
            .setClientID(getDefaultStringMetadataInfo(headers, GrpcConstants.CLIENT_ID))
            .setProtocolType(ChannelProtocolType.GRPC_V2.getName())
            .setLanguage(getDefaultStringMetadataInfo(headers, GrpcConstants.LANGUAGE))
            .setClientVersion(getDefaultStringMetadataInfo(headers, GrpcConstants.CLIENT_VERSION))
            .setAction(getDefaultStringMetadataInfo(headers, GrpcConstants.SIMPLE_RPC_NAME))
            .setNamespace(getDefaultStringMetadataInfo(headers, GrpcConstants.NAMESPACE_ID));

        if (ctx.getDeadline() != null) {
            context.setRemainingMs(ctx.getDeadline().timeRemaining(TimeUnit.MILLISECONDS));
        }
    }

    protected String getDefaultStringMetadataInfo(Metadata headers, Metadata.Key<String> key) {
        return StringUtil.defaultString(headers.get(key));
    }
}
