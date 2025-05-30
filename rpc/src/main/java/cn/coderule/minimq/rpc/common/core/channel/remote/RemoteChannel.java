
package cn.coderule.minimq.rpc.common.core.channel.remote;

import cn.coderule.minimq.rpc.common.core.channel.ChannelExtendAttributeGetter;
import cn.coderule.minimq.rpc.common.core.channel.mock.MockChannel;
import cn.coderule.minimq.rpc.common.core.enums.ChannelProtocolType;
import com.google.common.base.MoreObjects;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

public class RemoteChannel extends MockChannel implements ChannelExtendAttributeGetter {
    protected final ChannelProtocolType type;
    protected final String remoteProxyIp;
    protected volatile String extendAttribute;

    public RemoteChannel(String remoteProxyIp, String remoteAddress, String localAddress, ChannelProtocolType type, String extendAttribute) {
        super(null,
            new RemoteChannelId(remoteProxyIp, remoteAddress, localAddress, type),
            remoteAddress, localAddress);
        this.type = type;
        this.remoteProxyIp = remoteProxyIp;
        this.extendAttribute = extendAttribute;
    }

    public static class RemoteChannelId implements ChannelId {

        private final String id;

        public RemoteChannelId(String remoteProxyIp, String remoteAddress, String localAddress, ChannelProtocolType type) {
            this.id = remoteProxyIp + "@" + remoteAddress + "@" + localAddress + "@" + type;
        }

        @Override
        public String asShortText() {
            return this.id;
        }

        @Override
        public String asLongText() {
            return this.id;
        }

        @Override
        public int compareTo(ChannelId o) {
            return this.id.compareTo(o.asLongText());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("id", id)
                .toString();
        }
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    public ChannelProtocolType getType() {
        return type;
    }

    public String encode() {
        return RemoteChannelSerializer.toJson(this);
    }

    public static RemoteChannel decode(String data) {
        return RemoteChannelSerializer.decodeFromJson(data);
    }

    public static RemoteChannel create(Channel channel) {
        if (channel instanceof RemoteChannelConverter) {
            return ((RemoteChannelConverter) channel).toRemoteChannel();
        }
        return null;
    }

    public String getRemoteProxyIp() {
        return remoteProxyIp;
    }

    public void setExtendAttribute(String extendAttribute) {
        this.extendAttribute = extendAttribute;
    }

    @Override
    public String getChannelExtendAttribute() {
        return this.extendAttribute;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("channelId", id())
            .add("type", type)
            .add("remoteProxyIp", remoteProxyIp)
            .add("extendAttribute", extendAttribute)
            .toString();
    }
}
