
package cn.coderule.minimq.broker.server.rpc;

import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.broker.server.core.ChannelHelper;
import cn.coderule.minimq.domain.domain.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.broker.server.core.ClientChannel;
import cn.coderule.minimq.rpc.common.core.channel.ChannelExtendAttributeGetter;
import cn.coderule.minimq.rpc.common.core.channel.ChannelProtocolType;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.MoreObjects;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import java.time.Duration;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RpcChannel extends ClientChannel {
    private static final long DEFAULT_MQ_CLIENT_TIMEOUT = Duration.ofSeconds(3).toMillis();
    private final String clientId;
    private final String remoteAddress;
    private final String localAddress;
    private final Set<SubscriptionData> subscriptionData;

    @Getter @Setter
    private RelayService relayService;

    public RpcChannel(Channel parent, String clientId, Set<SubscriptionData> subscriptionData) {
        super(parent, parent.id(),
            NetworkUtil.toString(parent.remoteAddress()),
            NetworkUtil.toString(parent.localAddress())
        );
        this.clientId = clientId;
        this.remoteAddress = NetworkUtil.toString(parent.remoteAddress());
        this.localAddress = NetworkUtil.toString(parent.localAddress());
        this.subscriptionData = subscriptionData;
    }

    @Override
    public boolean isOpen() {
        return this.parent().isOpen();
    }

    @Override
    public boolean isActive() {
        return this.parent().isActive();
    }

    @Override
    public boolean isWritable() {
        return this.parent().isWritable();
    }

    @Override
    public ChannelFuture close() {
        return this.parent().close();
    }

    @Override
    public ChannelConfig config() {
        return this.parent().config();
    }

    @Override
    public ChannelMetadata metadata() {
        return this.parent().metadata();
    }


    public String getClientId() {
        return clientId;
    }


    public static Set<SubscriptionData> parseChannelExtendAttribute(Channel channel) {
        if (!ChannelHelper.getChannelProtocolType(channel).equals(ChannelProtocolType.REMOTING)) {
            return null;
        }

        if (!(channel instanceof ChannelExtendAttributeGetter)) {
            return null;
        }

        String attr = ((ChannelExtendAttributeGetter) channel).getChannelExtendAttribute();
        if (attr == null) {
            return null;
        }

        try {
            return JSON.parseObject(attr, new TypeReference<Set<SubscriptionData>>() {
            });
        } catch (Exception e) {
            log.error("convert remoting extend attribute to subscriptionDataSet failed. data:{}", attr, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("parent", parent())
            .add("clientId", clientId)
            .add("remoteAddress", remoteAddress)
            .add("localAddress", localAddress)
            .add("subscriptionData", subscriptionData)
            .toString();
    }
}
