package cn.coderule.minimq.registry.server;

import cn.coderule.minimq.rpc.common.RpcHook;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;

public class ZoneHook implements RpcHook {
    @Override
    public void onRequestStart(RpcContext ctx, RpcCommand request) {

    }

    @Override
    public void onResponseComplete(RpcContext ctx, RpcCommand request, RpcCommand response) {

    }
}
