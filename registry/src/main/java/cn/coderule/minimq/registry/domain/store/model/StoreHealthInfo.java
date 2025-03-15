package cn.coderule.minimq.registry.domain.store.model;

import cn.coderule.minimq.rpc.common.protocol.DataVersion;
import io.netty.channel.Channel;
import java.io.Serializable;
import lombok.Data;

@Data
public class StoreHealthInfo implements Serializable {
    private long lastUpdateTimestamp;
    private long heartbeatTimeoutMillis;
    private DataVersion dataVersion;
    private Channel channel;
    private String haServerAddr;
}
