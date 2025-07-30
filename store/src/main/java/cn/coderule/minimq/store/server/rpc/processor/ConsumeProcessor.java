package cn.coderule.minimq.store.server.rpc.processor;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.service.store.api.meta.ConsumeOffsetStore;
import cn.coderule.minimq.rpc.common.rpc.RpcProcessor;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumeProcessor implements RpcProcessor {
    private final ConsumeOffsetStore offsetStore;
    @Getter
    private final ExecutorService executor;
    @Getter
    private final Set<Integer> codeSet = Set.of(
        RequestCode.QUERY_CONSUMER_OFFSET,
        RequestCode.UPDATE_CONSUMER_OFFSET,
        RequestCode.GET_ALL_CONSUMER_OFFSET
    );

    public ConsumeProcessor(ConsumeOffsetStore offsetStore, ExecutorService executor) {
        this.offsetStore = offsetStore;
        this.executor = executor;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return switch (request.getCode()) {
            case RequestCode.UPDATE_CONSUMER_OFFSET -> this.updateConsumeOffset(ctx, request);
            case RequestCode.QUERY_CONSUMER_OFFSET -> this.getConsumeOffset(ctx, request);
            case RequestCode.GET_ALL_CONSUMER_OFFSET -> this.getAllConsumeOffset(ctx, request);
            default -> this.unsupportedCode(ctx, request);
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand getConsumeOffset(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }

    private RpcCommand updateConsumeOffset(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }

    private RpcCommand getAllConsumeOffset(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);

        String data = offsetStore.getAllOffsetJson();

        if (StringUtil.isBlank(data)) {
            log.error("No consumeOffset in this server, client:{}", ctx.channel().remoteAddress());
            return response.failure(
                ResponseCode.SYSTEM_ERROR,
                "No consumeOffset in this server"
            );
        }

        try {
            response.setBody(data.getBytes(MQConstants.MQ_CHARSET));
            return response.success();
        } catch (Exception e) {
            log.error("getAllConsumeOffset error", e);
            return response.failure(
                ResponseCode.SYSTEM_ERROR,
                "UnsupportedEncodingException " + e.getMessage()
            );
        }

    }
}
