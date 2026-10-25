package cn.coderule.wolfmq.domain.domain.producer;

import cn.coderule.wolfmq.domain.server.ClientChannelInfo;
import java.io.Serializable;
import lombok.Data;

@Data
public class ProducerInfo implements Serializable {
    private String groupName;
    private ClientChannelInfo channelInfo;
}
