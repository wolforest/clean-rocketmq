package cn.coderule.minimq.registry.processor;

import cn.coderule.minimq.registry.domain.kv.KVService;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.protocol.code.SystemResponseCode;
import cn.coderule.minimq.rpc.registry.protocol.header.PutKVConfigRequestHeader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KVProcessor implements RpcProcessor {
    private final KVService kvService;

    public KVProcessor(KVService kvService) {
        this.kvService = kvService;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return switch (request.getCode()) {
            case RequestCode.PUT_KV_CONFIG -> this.putKVConfig(ctx, request);
            case RequestCode.GET_KV_CONFIG -> this.getKVConfig(ctx, request);
            case RequestCode.DELETE_KV_CONFIG -> this.deleteKVConfig(ctx, request);
            default -> {
                String error = " request type " + request.getCode() + " not supported";
                yield RpcCommand.createResponseCommand(SystemResponseCode.REQUEST_CODE_NOT_SUPPORTED, error);
            }
        };
    }

    @Override
    public boolean reject() {
        return false;
    }

    private RpcCommand putKVConfig(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RpcCommand response = RpcCommand.createResponseCommand(null);
        PutKVConfigRequestHeader requestHeader = request.decodeHeader(PutKVConfigRequestHeader.class);

        if (null == requestHeader.getNamespace() || null == requestHeader.getKey()) {
            return response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "namespace or key is null");
        }

        kvService.putKVConfig(requestHeader.getNamespace(), requestHeader.getKey(), requestHeader.getValue());

        return response.setCodeAndRemark(SystemResponseCode.SUCCESS, null);
    }

    private RpcCommand getKVConfig(RpcContext ctx, RpcCommand request) {
        return null;
    }

    private RpcCommand deleteKVConfig(RpcContext ctx, RpcCommand request) {
        return null;
    }

}
