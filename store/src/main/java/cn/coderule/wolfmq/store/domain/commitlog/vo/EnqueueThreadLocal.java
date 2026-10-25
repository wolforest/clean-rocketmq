package cn.coderule.wolfmq.store.domain.commitlog.vo;

import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.domain.message.codec.MessageEncoder;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class EnqueueThreadLocal implements Serializable {
    private final MessageEncoder encoder;

    public EnqueueThreadLocal(MessageConfig config) {
        encoder = new MessageEncoder(config);
    }
}
