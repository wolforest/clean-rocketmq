package cn.coderule.minimq.store.domain.commitlog.vo;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.message.MessageEncoder;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class EnqueueThreadLocal implements Serializable {
    private final MessageEncoder encoder;

    public EnqueueThreadLocal(MessageConfig config) {
        encoder = new MessageEncoder(config);
    }
}
