
package cn.coderule.minimq.rpc.common.core.channel;

import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.rpc.common.core.channel.mock.MockChannel;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.EventLoop;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ClientChannel extends MockChannel {
    protected final SocketAddress remoteSocketAddress;
    protected final SocketAddress localSocketAddress;

    protected ClientChannel(Channel parent, String remoteAddress,
        String localAddress) {
        super(parent, remoteAddress, localAddress);
        this.remoteSocketAddress = NetworkUtil.toSocketAddress(remoteAddress);
        this.localSocketAddress = NetworkUtil.toSocketAddress(localAddress);
    }

    protected ClientChannel(Channel parent, ChannelId id, String remoteAddress,
        String localAddress) {
        super(parent, id, remoteAddress, localAddress);
        this.remoteSocketAddress = NetworkUtil.toSocketAddress(remoteAddress);
        this.localSocketAddress = NetworkUtil.toSocketAddress(localAddress);
    }

    public abstract RelayService getRelayService();

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public ChannelMetadata metadata() {
        return null;
    }

    @Override
    protected io.netty.channel.AbstractChannel.AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return false;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {

    }

    @Override
    protected void doDisconnect() throws Exception {

    }

    @Override
    protected void doClose() throws Exception {

    }

    @Override
    protected void doBeginRead() throws Exception {

    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {

    }

    @Override
    protected SocketAddress localAddress0() {
        return this.localSocketAddress;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return this.remoteSocketAddress;
    }
}
