package cn.coderule.minimq.domain.domain.model.cluster;

import cn.coderule.minimq.domain.domain.enums.code.LanguageCode;
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
public class ClientChannelInfo implements Serializable {
    private Channel channel;
    private String clientId;
    private LanguageCode language;
    private int version;
    private volatile long lastUpdateTime = System.currentTimeMillis();

    public ClientChannelInfo(Channel channel) {
        this(channel, null, null, 0);
    }

    public ClientChannelInfo(Channel channel, String clientId, LanguageCode language, int version) {
        this.channel = channel;
        this.clientId = clientId;
        this.language = language;
        this.version = version;
    }
}
