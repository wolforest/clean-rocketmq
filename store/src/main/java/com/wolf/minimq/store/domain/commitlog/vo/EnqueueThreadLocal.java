package com.wolf.minimq.store.domain.commitlog.vo;

import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.utils.MessageEncoder;
import lombok.Getter;

@Getter
public class EnqueueThreadLocal {
    private final MessageEncoder encoder;

    public EnqueueThreadLocal(MessageConfig config) {
        encoder = new MessageEncoder(config);
    }
}
