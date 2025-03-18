package cn.coderule.minimq.registry.api;

import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigProcessor implements RpcProcessor {
    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) {
        return null;
    }

    @Override
    public boolean reject() {
        return false;
    }
}
