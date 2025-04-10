package cn.coderule.minimq.registry.domain.store.model;

import cn.coderule.minimq.domain.domain.model.DataVersion;
import io.netty.channel.Channel;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreHealthInfo implements Serializable {
    private long lastUpdateTimestamp;
    private long heartbeatTimeoutMillis;
    private DataVersion dataVersion;
    private Channel channel;
    private String haServerAddr;
}
