package cn.coderule.minimq.rpc.broker.protocol.producer;

import cn.coderule.minimq.rpc.common.core.channel.ClientChannelInfo;
import java.io.Serializable;
import lombok.Data;

@Data
public class ProducerInfo implements Serializable {
    private String groupName;
    private ClientChannelInfo channelInfo;
}
