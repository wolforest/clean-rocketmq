package cn.coderule.minimq.registry.processor;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.store.StoreRegistry;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.netty.service.NettyHelper;
import cn.coderule.minimq.rpc.common.protocol.code.SystemResponseCode;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.header.RegisterBrokerResponseHeader;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryProcessor implements RpcProcessor {
    private RegistryConfig registryConfig;
    private StoreRegistry storeRegistry;

    @Override
    public RpcCommand process(RpcContext ctx, RpcCommand request) {

        return null;
    }

    @Override
    public boolean reject() {
        return false;
    }

    private boolean checksum(ChannelHandlerContext ctx, RpcCommand request, RegisterBrokerRequestHeader requestHeader, RpcCommand response) {
        if (requestHeader.getBodyCrc32() == 0) {
            return true;
        }

        final int crc32 = HashUtil.crc32(request.getBody());
        if (crc32 != requestHeader.getBodyCrc32()) {

            log.warn(String.format("receive registerBroker request,crc32 not match,from %s",
                NettyHelper.getRemoteAddr(ctx.channel())));

            response.setCodeAndRemark(SystemResponseCode.SYSTEM_ERROR, "crc32 not match");
            return false;
        }

        return true;
    }

    public RpcCommand registerStore(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        RegisterBrokerRequestHeader requestHeader = request.decodeHeader(RegisterBrokerRequestHeader.class);
        RpcCommand response = RpcCommand.createResponseCommand(RegisterBrokerResponseHeader.class);
        RegisterBrokerResponseHeader responseHeader = (RegisterBrokerResponseHeader) response.readCustomHeader();

        if (!checksum(ctx.getChannelContext(), request, requestHeader, response)) {
            return response;
        }



        return null;
    }

    public RpcCommand unregisterStore(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }

    public RpcCommand registerTopic(RpcContext ctx, RpcCommand request) throws RemotingCommandException {
        return null;
    }


}
