package cn.coderule.minimq.registry.processor;

import cn.coderule.minimq.registry.domain.property.PropertyService;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.protocol.code.SystemResponseCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyProcessor implements RpcProcessor {
    private final PropertyService propertyService;

    public PropertyProcessor(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) {
        return switch (request.getCode()) {
            case RequestCode.UPDATE_NAMESRV_CONFIG -> this.setProperty(ctx, request);
            case RequestCode.GET_NAMESRV_CONFIG -> this.getProperty(ctx, request);
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

    private RpcCommand getProperty(RpcContext ctx, RpcCommand request) {
        return null;
    }

    private RpcCommand setProperty(RpcContext ctx, RpcCommand request) {
        return null;
    }

}
