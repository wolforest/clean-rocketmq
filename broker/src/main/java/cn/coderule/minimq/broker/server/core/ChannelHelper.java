
package cn.coderule.minimq.broker.server.core;

import cn.coderule.minimq.broker.server.grpc.service.channel.GrpcChannel;
import cn.coderule.minimq.broker.server.rpc.RpcChannel;
import cn.coderule.minimq.rpc.common.core.channel.remote.RemoteChannel;
import cn.coderule.minimq.rpc.common.core.enums.ChannelProtocolType;
import io.netty.channel.Channel;

public class ChannelHelper {

    /**
     * judge channel is sync from other proxy or not
     *
     * @param channel channel
     * @return true if is sync from other proxy
     */
    public static boolean isRemote(Channel channel) {
        return channel instanceof RemoteChannel;
    }

    public static ChannelProtocolType getChannelProtocolType(Channel channel) {
        if (channel instanceof GrpcChannel) {
            return ChannelProtocolType.GRPC_V2;
        } else if (channel instanceof RpcChannel) {
            return ChannelProtocolType.REMOTING;
        } else if (channel instanceof RemoteChannel remoteChannel) {
            return remoteChannel.getType();
        }
        return ChannelProtocolType.UNKNOWN;
    }
}
