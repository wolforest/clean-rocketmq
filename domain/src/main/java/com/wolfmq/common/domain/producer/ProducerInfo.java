package com.wolfmq.common.domain.producer;

import com.wolfmq.common.server.ClientChannelInfo;
import java.io.Serializable;
import lombok.Data;

@Data
public class ProducerInfo implements Serializable {
    private String groupName;
    private ClientChannelInfo channelInfo;
}
