package cn.coderule.minimq.rpc.common.grpc.core;

import cn.coderule.common.util.lang.bean.BeanUtil;
import io.netty.channel.ChannelId;

public class GrpcChannelId implements ChannelId {
    private final String clientId;

    public GrpcChannelId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String asShortText() {
        return this.clientId;
    }

    @Override
    public String asLongText() {
        return this.clientId;
    }

    @Override
    public int compareTo(ChannelId o) {
        if (this == o) {
            return 0;
        }

        if (o instanceof GrpcChannelId other) {
            return BeanUtil.compareToBuilder()
                .append(this.clientId, other.clientId)
                .toComparison();
        }

        return asLongText().compareTo(
            o.asLongText()
        );
    }
}
