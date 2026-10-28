package com.wolfmq.store.domain.commitlog.vo;

import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.domain.message.codec.MessageEncoder;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class EnqueueThreadLocal implements Serializable {
    private final MessageEncoder encoder;

    public EnqueueThreadLocal(MessageConfig config) {
        encoder = new MessageEncoder(config);
    }
}
